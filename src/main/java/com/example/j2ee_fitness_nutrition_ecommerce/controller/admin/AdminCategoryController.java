package com.example.j2ee_fitness_nutrition_ecommerce.controller.admin;

import com.example.j2ee_fitness_nutrition_ecommerce.dto.CategoryRequest;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.Category;
import com.example.j2ee_fitness_nutrition_ecommerce.service.CategoryService;
import com.example.j2ee_fitness_nutrition_ecommerce.service.FileStorageService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/categories")
public class AdminCategoryController {

    private final CategoryService categoryService;
    private final FileStorageService fileStorageService;

    public AdminCategoryController(CategoryService categoryService, FileStorageService fileStorageService) {
        this.categoryService = categoryService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("categories", categoryService.findAll());
        return "admin/category/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("category", new CategoryRequest());
        return "admin/category/form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("category") CategoryRequest request,
                       BindingResult result,
                       @RequestParam(required = false) MultipartFile imageFile,
                       RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/category/form";
        }

        Category category;
        if (request.getId() != null) {
            category = categoryService.findById(request.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        } else {
            category = new Category();
        }

        category.setName(request.getName());
        category.setSlug(request.getSlug());
        category.setDescription(request.getDescription());
        category.setActive(request.isActive());

        if (imageFile != null && !imageFile.isEmpty()) {
            String imageUrl = fileStorageService.store(imageFile, "categories");
            category.setImageUrl(imageUrl);
        } else if (request.getImageUrl() != null) {
            category.setImageUrl(request.getImageUrl());
        }

        categoryService.save(category);
        redirectAttributes.addFlashAttribute("success", "Category saved successfully!");
        return "redirect:/admin/categories";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Category category = categoryService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        CategoryRequest request = new CategoryRequest();
        request.setId(category.getId());
        request.setName(category.getName());
        request.setSlug(category.getSlug());
        request.setDescription(category.getDescription());
        request.setImageUrl(category.getImageUrl());
        request.setActive(category.isActive());

        model.addAttribute("category", request);
        return "admin/category/form";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        categoryService.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "Category deactivated!");
        return "redirect:/admin/categories";
    }
}
