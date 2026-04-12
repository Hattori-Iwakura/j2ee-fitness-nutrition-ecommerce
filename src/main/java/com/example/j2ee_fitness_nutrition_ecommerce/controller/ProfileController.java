package com.example.j2ee_fitness_nutrition_ecommerce.controller;

import com.example.j2ee_fitness_nutrition_ecommerce.dto.ChangePasswordRequest;
import com.example.j2ee_fitness_nutrition_ecommerce.dto.ProfileUpdateRequest;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.User;
import com.example.j2ee_fitness_nutrition_ecommerce.service.UserService;
import com.example.j2ee_fitness_nutrition_ecommerce.util.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String profile(Authentication authentication, Model model) {
        String email = SecurityUtils.requireUserEmail(authentication);
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        ProfileUpdateRequest profileRequest = new ProfileUpdateRequest();
        profileRequest.setFullName(user.getFullName());
        profileRequest.setPhone(user.getPhone());
        profileRequest.setAddress(user.getAddress());

        model.addAttribute("user", user);
        model.addAttribute("profileRequest", profileRequest);
        model.addAttribute("passwordRequest", new ChangePasswordRequest());
        return "profile/index";
    }

    @PostMapping("/update")
    public String updateProfile(Authentication authentication,
                                @Valid @ModelAttribute("profileRequest") ProfileUpdateRequest request,
                                BindingResult result,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        String email = SecurityUtils.requireUserEmail(authentication);
        if (result.hasErrors()) {
            User user = userService.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            model.addAttribute("user", user);
            model.addAttribute("passwordRequest", new ChangePasswordRequest());
            return "profile/index";
        }

        userService.updateProfile(email, request);
        redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
        return "redirect:/profile";
    }

    @PostMapping("/change-password")
    public String changePassword(Authentication authentication,
                                 @Valid @ModelAttribute("passwordRequest") ChangePasswordRequest request,
                                 BindingResult result,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        String email = SecurityUtils.requireUserEmail(authentication);
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            result.rejectValue("confirmNewPassword", "error.confirmNewPassword", "Passwords do not match");
        }

        if (result.hasErrors()) {
            User user = userService.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            model.addAttribute("user", user);
            ProfileUpdateRequest profileRequest = new ProfileUpdateRequest();
            profileRequest.setFullName(user.getFullName());
            profileRequest.setPhone(user.getPhone());
            profileRequest.setAddress(user.getAddress());
            model.addAttribute("profileRequest", profileRequest);
            return "profile/index";
        }

        try {
            userService.changePassword(email, request);
            redirectAttributes.addFlashAttribute("success", "Password changed successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/profile";
    }
}
