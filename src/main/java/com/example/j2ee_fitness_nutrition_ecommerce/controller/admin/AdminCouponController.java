package com.example.j2ee_fitness_nutrition_ecommerce.controller.admin;

import com.example.j2ee_fitness_nutrition_ecommerce.entity.Coupon;
import com.example.j2ee_fitness_nutrition_ecommerce.enums.DiscountType;
import com.example.j2ee_fitness_nutrition_ecommerce.service.CouponService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/coupons")
public class AdminCouponController {

    private final CouponService couponService;

    public AdminCouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("coupons", couponService.findAll());
        return "admin/coupon/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("coupon", new Coupon());
        model.addAttribute("discountTypes", DiscountType.values());
        return "admin/coupon/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Coupon coupon, RedirectAttributes redirectAttributes) {
        couponService.save(coupon);
        redirectAttributes.addFlashAttribute("success", "Coupon saved successfully!");
        return "redirect:/admin/coupons";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Coupon coupon = couponService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Coupon not found"));
        model.addAttribute("coupon", coupon);
        model.addAttribute("discountTypes", DiscountType.values());
        return "admin/coupon/form";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        couponService.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "Coupon deactivated!");
        return "redirect:/admin/coupons";
    }
}
