package com.example.j2ee_fitness_nutrition_ecommerce.repository;

import com.example.j2ee_fitness_nutrition_ecommerce.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    long countByCategory_Id(Long categoryId);

    Page<Product> findByActiveTrue(Pageable pageable);
    Page<Product> findByCategoryIdAndActiveTrue(Long categoryId, Pageable pageable);
    Page<Product> findByNameContainingIgnoreCaseAndActiveTrue(String keyword, Pageable pageable);
    Optional<Product> findBySlug(String slug);
    Optional<Product> findBySlugAndActiveTrue(String slug);

    @Query("SELECT DISTINCT p.brand FROM Product p WHERE p.active = true AND p.brand IS NOT NULL ORDER BY p.brand")
    List<String> findDistinctBrands();

    // Related products: same category, excluding current product
    List<Product> findTop4ByCategoryIdAndActiveTrueAndIdNot(Long categoryId, Long productId);

    // Co-purchased products: other products bought in the same order (JPQL-friendly: no ORDER BY inside IN subquery)
    @Query("""
        SELECT od2.variant.product FROM OrderDetail od1
        JOIN od1.order o
        JOIN o.orderDetails od2
        WHERE od1.variant.product.id = :productId
          AND od2.variant.product.id <> :productId
          AND od2.variant.product.active = true
        GROUP BY od2.variant.product
        ORDER BY COUNT(od2.id) DESC
        """)
    List<Product> findCoPurchasedProducts(@Param("productId") Long productId, Pageable pageable);

    // Best sellers: products with highest total quantity sold
    @Query("""
        SELECT od.variant.product FROM OrderDetail od
        WHERE od.variant.product.active = true
        GROUP BY od.variant.product
        ORDER BY SUM(od.quantity) DESC
        """)
    List<Product> findBestSellers(Pageable pageable);

    /** Newest active products (for home “featured / new arrivals”). */
    List<Product> findByActiveTrueOrderByCreatedAtDesc(Pageable pageable);
}
