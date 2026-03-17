package com.example.j2ee_fitness_nutrition_ecommerce.controller.admin;

import com.example.j2ee_fitness_nutrition_ecommerce.entity.Product;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.ProductVariant;
import com.example.j2ee_fitness_nutrition_ecommerce.repository.ProductVariantRepository;
import com.example.j2ee_fitness_nutrition_ecommerce.service.CategoryService;
import com.example.j2ee_fitness_nutrition_ecommerce.service.FileStorageService;
import com.example.j2ee_fitness_nutrition_ecommerce.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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

    public AdminProductController(ProductService productService,
                                   CategoryService categoryService,
                                   ProductVariantRepository variantRepository,
                                   FileStorageService fileStorageService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.variantRepository = variantRepository;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("products", productService.findAll());
        return "admin/product/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.findAllActive());
        return "admin/product/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Product product,
                       @RequestParam Long categoryId,
                       @RequestParam(required = false) MultipartFile imageFile,
                       RedirectAttributes redirectAttributes) {
        var category = categoryService.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        product.setCategory(category);
        if (imageFile != null && !imageFile.isEmpty()) {
            String imageUrl = fileStorageService.store(imageFile, "products");
            product.setImageUrl(imageUrl);
        }
        productService.save(product);
        redirectAttributes.addFlashAttribute("success", "Product saved successfully!");
        return "redirect:/admin/products";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Product product = productService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        model.addAttribute("product", product);
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
        model.addAttribute("variant", new ProductVariant());
        return "admin/product/variants";
    }

    @PostMapping("/{productId}/variants/save")
    public String saveVariant(@PathVariable Long productId,
                              @ModelAttribute ProductVariant variant,
                              RedirectAttributes redirectAttributes) {
        Product product = productService.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        variant.setProduct(product);
        variantRepository.save(variant);
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
