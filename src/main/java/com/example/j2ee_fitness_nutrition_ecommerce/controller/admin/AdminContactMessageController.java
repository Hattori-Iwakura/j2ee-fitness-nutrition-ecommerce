package com.example.j2ee_fitness_nutrition_ecommerce.controller.admin;

import com.example.j2ee_fitness_nutrition_ecommerce.entity.ContactMessage;
import com.example.j2ee_fitness_nutrition_ecommerce.service.ContactMessageService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/contact-messages")
public class AdminContactMessageController {

    private final ContactMessageService contactMessageService;

    public AdminContactMessageController(ContactMessageService contactMessageService) {
        this.contactMessageService = contactMessageService;
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                       Model model) {
        Pageable pageable = PageRequest.of(page, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ContactMessage> messages = contactMessageService.findAll(pageable);
        model.addAttribute("messages", messages);
        model.addAttribute("unreadCount", contactMessageService.countUnread());
        return "admin/contact/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        ContactMessage msg = contactMessageService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));
        contactMessageService.markAsRead(id);
        msg.setRead(true);
        model.addAttribute("message", msg);
        return "admin/contact/detail";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        contactMessageService.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "Message deleted.");
        return "redirect:/admin/contact-messages";
    }
}
