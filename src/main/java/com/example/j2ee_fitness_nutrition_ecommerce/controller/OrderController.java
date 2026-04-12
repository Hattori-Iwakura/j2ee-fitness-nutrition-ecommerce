package com.example.j2ee_fitness_nutrition_ecommerce.controller;

import com.example.j2ee_fitness_nutrition_ecommerce.entity.Order;
import com.example.j2ee_fitness_nutrition_ecommerce.service.OrderService;
import com.example.j2ee_fitness_nutrition_ecommerce.util.SecurityUtils;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public String myOrders(Authentication authentication, Model model) {
        String email = SecurityUtils.requireUserEmail(authentication);
        model.addAttribute("orders", orderService.findByUserEmail(email));
        return "order/list";
    }

    @GetMapping("/{id}")
    public String orderDetail(@PathVariable Long id,
                              Authentication authentication,
                              Model model) {
        String email = SecurityUtils.requireUserEmail(authentication);
        Order order = orderService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        // Verify the order belongs to the authenticated user
        if (!order.getUser().getEmail().equals(email)) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied");
        }

        model.addAttribute("order", order);
        return "order/detail";
    }
}
