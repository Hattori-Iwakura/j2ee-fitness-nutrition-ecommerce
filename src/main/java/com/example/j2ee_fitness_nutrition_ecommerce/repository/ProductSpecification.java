package com.example.j2ee_fitness_nutrition_ecommerce.repository;

import com.example.j2ee_fitness_nutrition_ecommerce.dto.ProductFilter;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.Product;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.ProductVariant;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public final class ProductSpecification {

    private ProductSpecification() {}

    public static Specification<Product> withFilter(ProductFilter filter, Long categoryId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Only active products
            predicates.add(cb.isTrue(root.get("active")));

            // Category filter
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }

            // Keyword search (name or description)
            if (filter.getKeyword() != null && !filter.getKeyword().isBlank()) {
                String pattern = "%" + filter.getKeyword().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern)
                ));
            }

            // Brand filter
            if (filter.getBrands() != null && !filter.getBrands().isEmpty()) {
                predicates.add(root.get("brand").in(filter.getBrands()));
            }

            // Price range and in-stock filters use a subquery on variants
            if (filter.getMinPrice() != null || filter.getMaxPrice() != null || filter.isInStockOnly()) {
                Subquery<Long> variantSubquery = query.subquery(Long.class);
                Root<ProductVariant> variantRoot = variantSubquery.from(ProductVariant.class);
                List<Predicate> variantPredicates = new ArrayList<>();

                variantPredicates.add(cb.equal(variantRoot.get("product"), root));
                variantPredicates.add(cb.isTrue(variantRoot.get("active")));

                if (filter.getMinPrice() != null) {
                    variantPredicates.add(cb.greaterThanOrEqualTo(variantRoot.get("price"), filter.getMinPrice()));
                }
                if (filter.getMaxPrice() != null) {
                    variantPredicates.add(cb.lessThanOrEqualTo(variantRoot.get("price"), filter.getMaxPrice()));
                }
                if (filter.isInStockOnly()) {
                    variantPredicates.add(cb.greaterThan(variantRoot.get("stock"), 0));
                }

                variantSubquery.select(variantRoot.get("id"))
                        .where(variantPredicates.toArray(new Predicate[0]));
                predicates.add(cb.exists(variantSubquery));
            }

            // Price-based sorting: use a subquery for min variant price
            // Only apply for main queries (not count queries)
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                String sort = filter.getSort();
                if ("price-asc".equals(sort) || "price-desc".equals(sort)) {
                    Subquery<BigDecimal> priceSubquery = query.subquery(BigDecimal.class);
                    Root<ProductVariant> priceRoot = priceSubquery.from(ProductVariant.class);
                    priceSubquery.select(cb.least(priceRoot.<BigDecimal>get("price")))
                            .where(
                                    cb.equal(priceRoot.get("product"), root),
                                    cb.isTrue(priceRoot.get("active"))
                            );

                    if ("price-asc".equals(sort)) {
                        query.orderBy(cb.asc(priceSubquery));
                    } else {
                        query.orderBy(cb.desc(priceSubquery));
                    }
                }
            }

            query.distinct(true);

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
