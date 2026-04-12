package com.example.j2ee_fitness_nutrition_ecommerce.controller.admin;

import com.example.j2ee_fitness_nutrition_ecommerce.dto.ProductRequest;
import com.example.j2ee_fitness_nutrition_ecommerce.dto.ProductVariantRequest;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.Product;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.ProductVariant;
import com.example.j2ee_fitness_nutrition_ecommerce.enums.StockChangeType;
import com.example.j2ee_fitness_nutrition_ecommerce.repository.ProductVariantRepository;
import com.example.j2ee_fitness_nutrition_ecommerce.service.CategoryService;
import com.example.j2ee_fitness_nutrition_ecommerce.service.FileStorageService;
import com.example.j2ee_fitness_nutrition_ecommerce.service.ProductService;
import com.example.j2ee_fitness_nutrition_ecommerce.service.StockLogService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/products")
public class AdminProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final ProductVariantRepository variantRepository;
    private final FileStorageService fileStorageService;
    private final StockLogService stockLogService;

    public AdminProductController(ProductService productService,
                                   CategoryService categoryService,
                                   ProductVariantRepository variantRepository,
                                   FileStorageService fileStorageService,
                                   StockLogService stockLogService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.variantRepository = variantRepository;
        this.fileStorageService = fileStorageService;
        this.stockLogService = stockLogService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("products", productService.findAll());
        return "admin/product/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("product", new ProductRequest());
        model.addAttribute("categories", categoryService.findAllActive());
        return "admin/product/form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("product") ProductRequest request,
                       BindingResult result,
                       @RequestParam(required = false) MultipartFile imageFile,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.findAllActive());
            return "admin/product/form";
        }

        Product product;
        if (request.getId() != null) {
            product = productService.findById(request.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        } else {
            product = new Product();
        }

        product.setName(request.getName());
        product.setSlug(request.getSlug());
        product.setDescription(request.getDescription());
        product.setBrand(request.getBrand());
        product.setActive(request.isActive());

        var category = categoryService.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        product.setCategory(category);

        if (imageFile != null && !imageFile.isEmpty()) {
            String imageUrl = fileStorageService.store(imageFile, "products");
            product.setImageUrl(imageUrl);
        } else if (request.getImageUrl() != null) {
            product.setImageUrl(request.getImageUrl());
        }

        productService.save(product);
        redirectAttributes.addFlashAttribute("success", "Product saved successfully!");
        return "redirect:/admin/products";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Product product = productService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        ProductRequest request = new ProductRequest();
        request.setId(product.getId());
        request.setName(product.getName());
        request.setSlug(product.getSlug());
        request.setDescription(product.getDescription());
        request.setBrand(product.getBrand());
        request.setImageUrl(product.getImageUrl());
        request.setCategoryId(product.getCategory().getId());
        request.setActive(product.isActive());

        model.addAttribute("product", request);
        model.addAttribute("categories", categoryService.findAllActive());
        return "admin/product/form";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        productService.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "Product deactivated!");
        return "redirect:/admin/products";
    }

    // --- Variant Management ---

    @GetMapping("/{productId}/variants")
    public String variants(@PathVariable Long productId, Model model) {
        Product product = productService.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        model.addAttribute("product", product);
        model.addAttribute("variant", new ProductVariantRequest());
        return "admin/product/variants";
    }

    @PostMapping("/{productId}/variants/save")
    public String saveVariant(@PathVariable Long productId,
                              @Valid @ModelAttribute("variant") ProductVariantRequest request,
                              BindingResult result,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            Product product = productService.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found"));
            model.addAttribute("product", product);
            return "admin/product/variants";
        }

        Product product = productService.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        ProductVariant variant;
        int stockBefore = 0;
        boolean isNew = request.getId() == null;
        if (!isNew) {
            variant = variantRepository.findById(request.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Variant not found"));
            stockBefore = variant.getStock();
        } else {
            variant = new ProductVariant();
        }

        variant.setFlavor(request.getFlavor());
        variant.setWeight(request.getWeight());
        variant.setPrice(request.getPrice());
        variant.setStock(request.getStock());
        variant.setSku(request.getSku());
        variant.setProduct(product);

        variantRepository.save(variant);

        int stockAfter = variant.getStock();
        if (isNew) {
            stockLogService.log(variant, 0, stockAfter, StockChangeType.RESTOCK);
        } else if (stockAfter > stockBefore) {
            stockLogService.log(variant, stockBefore, stockAfter, StockChangeType.RESTOCK);
        } else if (stockAfter < stockBefore) {
            stockLogService.log(variant, stockBefore, stockAfter, StockChangeType.ADJUSTMENT);
        }
        redirectAttributes.addFlashAttribute("success", "Variant saved!");
        return "redirect:/admin/products/" + productId + "/variants";
    }

    @PostMapping("/{productId}/variants/delete/{variantId}")
    public String deleteVariant(@PathVariable Long productId,
                                @PathVariable Long variantId,
                                RedirectAttributes redirectAttributes) {
        var variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new IllegalArgumentException("Variant not found"));
        variant.setActive(false);
        variantRepository.save(variant);
        redirectAttributes.addFlashAttribute("success", "Variant deleted!");
        return "redirect:/admin/products/" + productId + "/variants";
    }
}
