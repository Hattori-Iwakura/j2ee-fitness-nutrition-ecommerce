package com.example.j2ee_fitness_nutrition_ecommerce.controller;

import com.example.j2ee_fitness_nutrition_ecommerce.dto.ContactForm;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.ContactMessage;
import com.example.j2ee_fitness_nutrition_ecommerce.service.ContactMessageService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ContactController {

    private final ContactMessageService contactMessageService;

    public ContactController(ContactMessageService contactMessageService) {
        this.contactMessageService = contactMessageService;
    }

    @GetMapping("/contact")
    public String form(Model model) {
        if (!model.containsAttribute("contact")) {
            model.addAttribute("contact", new ContactForm());
        }
        return "contact/index";
    }

    @PostMapping("/contact")
    public String submit(@Valid @ModelAttribute("contact") ContactForm form,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "contact/index";
        }
        ContactMessage msg = ContactMessage.builder()
                .fullName(form.getFullName().trim())
                .email(form.getEmail().trim())
                .phone(form.getPhone() != null ? form.getPhone().trim() : null)
                .message(form.getMessage().trim())
                .read(false)
                .build();
        contactMessageService.save(msg);
        redirectAttributes.addFlashAttribute("contactSuccess", true);
        return "redirect:/contact";
    }
}
