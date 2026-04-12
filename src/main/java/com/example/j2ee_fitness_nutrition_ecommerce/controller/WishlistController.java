package com.example.j2ee_fitness_nutrition_ecommerce.controller;

import com.example.j2ee_fitness_nutrition_ecommerce.service.WishlistService;
import com.example.j2ee_fitness_nutrition_ecommerce.util.SecurityUtils;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @GetMapping
    public String viewWishlist(Authentication authentication, Model model) {
        String email = SecurityUtils.requireUserEmail(authentication);
        model.addAttribute("wishlistItems", wishlistService.getWishlist(email));
        return "wishlist/index";
    }

    @PostMapping("/add")
    public String add(@RequestParam Long productId,
                      Authentication authentication,
                      @RequestHeader(value = "Referer", required = false) String referer,
                      RedirectAttributes redirectAttributes) {
        wishlistService.addToWishlist(SecurityUtils.requireUserEmail(authentication), productId);
        redirectAttributes.addFlashAttribute("success", "Added to wishlist!");
        return "redirect:" + sanitizeRedirect(referer);
    }

    @PostMapping("/remove")
    public String remove(@RequestParam Long productId,
                         Authentication authentication,
                         @RequestHeader(value = "Referer", required = false) String referer,
                         RedirectAttributes redirectAttributes) {
        wishlistService.removeFromWishlist(SecurityUtils.requireUserEmail(authentication), productId);
        redirectAttributes.addFlashAttribute("success", "Removed from wishlist.");
        return "redirect:" + sanitizeRedirect(referer);
    }

    private String sanitizeRedirect(String referer) {
        if (referer != null && referer.startsWith("/")) {
            return referer;
        }
        if (referer != null) {
            try {
                java.net.URI uri = java.net.URI.create(referer);
                String path = uri.getPath();
                return path != null ? path : "/wishlist";
            } catch (IllegalArgumentException e) {
                return "/wishlist";
            }
        }
        return "/wishlist";
    }
}
