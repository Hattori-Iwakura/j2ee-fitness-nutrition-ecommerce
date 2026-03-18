package com.example.j2ee_fitness_nutrition_ecommerce.controller.admin;

import com.example.j2ee_fitness_nutrition_ecommerce.dto.CouponRequest;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.Coupon;
import com.example.j2ee_fitness_nutrition_ecommerce.enums.DiscountType;
import com.example.j2ee_fitness_nutrition_ecommerce.service.CouponService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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
        model.addAttribute("coupon", new CouponRequest());
        model.addAttribute("discountTypes", DiscountType.values());
        return "admin/coupon/form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("coupon") CouponRequest request,
                       BindingResult result,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("discountTypes", DiscountType.values());
            return "admin/coupon/form";
        }

        Coupon coupon;
        if (request.getId() != null) {
            coupon = couponService.findById(request.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Coupon not found"));
        } else {
            coupon = new Coupon();
        }

        coupon.setCode(request.getCode().toUpperCase());
        coupon.setDiscountType(request.getDiscountType());
        coupon.setDiscountValue(request.getDiscountValue());
        coupon.setMinOrderAmount(request.getMinOrderAmount());
        coupon.setMaxUses(request.getMaxUses());
        coupon.setStartDate(request.getStartDate());
        coupon.setEndDate(request.getEndDate());
        coupon.setActive(request.isActive());

        couponService.save(coupon);
        redirectAttributes.addFlashAttribute("success", "Coupon saved successfully!");
        return "redirect:/admin/coupons";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Coupon coupon = couponService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Coupon not found"));

        CouponRequest request = new CouponRequest();
        request.setId(coupon.getId());
        request.setCode(coupon.getCode());
        request.setDiscountType(coupon.getDiscountType());
        request.setDiscountValue(coupon.getDiscountValue());
        request.setMinOrderAmount(coupon.getMinOrderAmount());
        request.setMaxUses(coupon.getMaxUses());
        request.setCurrentUses(coupon.getCurrentUses());
        request.setStartDate(coupon.getStartDate());
        request.setEndDate(coupon.getEndDate());
        request.setActive(coupon.isActive());

        model.addAttribute("coupon", request);
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
