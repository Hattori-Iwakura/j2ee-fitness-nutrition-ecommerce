package com.example.j2ee_fitness_nutrition_ecommerce.controller.admin;

import com.example.j2ee_fitness_nutrition_ecommerce.dto.BannerRequest;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.Banner;
import com.example.j2ee_fitness_nutrition_ecommerce.service.BannerService;
import com.example.j2ee_fitness_nutrition_ecommerce.service.FileStorageService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/banners")
public class AdminBannerController {

    private final BannerService bannerService;
    private final FileStorageService fileStorageService;

    public AdminBannerController(BannerService bannerService, FileStorageService fileStorageService) {
        this.bannerService = bannerService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("banners", bannerService.findAllForAdmin());
        return "admin/banner/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("banner", new BannerRequest());
        return "admin/banner/form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("banner") BannerRequest request,
                       BindingResult bindingResult,
                       @RequestParam(required = false) MultipartFile imageFile,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "admin/banner/form";
        }

        boolean hasFile = imageFile != null && !imageFile.isEmpty();
        boolean hasUrl = request.getImageUrl() != null && !request.getImageUrl().isBlank();
        if (request.getId() == null && !hasFile && !hasUrl) {
            model.addAttribute("banner", request);
            model.addAttribute("imageError", "Image is required (upload a file or provide image URL).");
            return "admin/banner/form";
        }

        Banner banner;
        if (request.getId() != null) {
            banner = bannerService.findById(request.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Banner not found"));
        } else {
            banner = new Banner();
        }

        if (hasFile) {
            banner.setImageUrl(fileStorageService.store(imageFile, "banners"));
        } else if (hasUrl) {
            banner.setImageUrl(request.getImageUrl().trim());
        }

        String link = request.getLinkUrl();
        banner.setLinkUrl((link == null || link.isBlank()) ? "#" : link.trim());
        banner.setActive(request.isActive());
        banner.setSortOrder(request.getSortOrder());

        bannerService.save(banner);
        redirectAttributes.addFlashAttribute("success", "Banner saved.");
        return "redirect:/admin/banners";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Banner b = bannerService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Banner not found"));
        BannerRequest req = new BannerRequest();
        req.setId(b.getId());
        req.setImageUrl(b.getImageUrl());
        req.setLinkUrl("#".equals(b.getLinkUrl()) ? "" : b.getLinkUrl());
        req.setActive(b.isActive());
        req.setSortOrder(b.getSortOrder());
        model.addAttribute("banner", req);
        return "admin/banner/form";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        bannerService.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "Banner deleted.");
        return "redirect:/admin/banners";
    }
}
