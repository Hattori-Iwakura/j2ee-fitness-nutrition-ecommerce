package com.example.j2ee_fitness_nutrition_ecommerce.controller;

import com.example.j2ee_fitness_nutrition_ecommerce.dto.CheckoutRequest;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.Order;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.User;
import com.example.j2ee_fitness_nutrition_ecommerce.service.CartItem;
import com.example.j2ee_fitness_nutrition_ecommerce.service.CartService;
import com.example.j2ee_fitness_nutrition_ecommerce.service.OrderService;
import com.example.j2ee_fitness_nutrition_ecommerce.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    private final CartService cartService;
    private final OrderService orderService;
    private final UserService userService;

    public CheckoutController(CartService cartService, OrderService orderService, UserService userService) {
        this.cartService = cartService;
        this.orderService = orderService;
        this.userService = userService;
    }

    @GetMapping
    public String checkoutPage(@AuthenticationPrincipal UserDetails userDetails,
                               HttpSession session, Model model) {
        List<CartItem> cartItems = cartService.getCart(session);
        if (cartItems.isEmpty()) {
            return "redirect:/cart";
        }

        CheckoutRequest checkoutRequest = new CheckoutRequest();
        User user = userService.findByEmail(userDetails.getUsername()).orElse(null);
        if (user != null) {
            checkoutRequest.setFullName(user.getFullName());
            checkoutRequest.setPhone(user.getPhone());
            checkoutRequest.setAddress(user.getAddress());
        }

        model.addAttribute("checkoutRequest", checkoutRequest);
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("cartTotal", cartService.getCartTotal(session));
        return "checkout/index";
    }

    @PostMapping
    public String placeOrder(@Valid @ModelAttribute CheckoutRequest checkoutRequest,
                             BindingResult result,
                             @AuthenticationPrincipal UserDetails userDetails,
                             HttpSession session, Model model,
                             RedirectAttributes redirectAttributes) {
        List<CartItem> cartItems = cartService.getCart(session);
        if (cartItems.isEmpty()) {
            return "redirect:/cart";
        }

        if (result.hasErrors()) {
            model.addAttribute("cartItems", cartItems);
            model.addAttribute("cartTotal", cartService.getCartTotal(session));
            return "checkout/index";
        }

        try {
            Order order = orderService.createOrder(userDetails.getUsername(), checkoutRequest, cartItems);
            cartService.clearCart(session);
            redirectAttributes.addFlashAttribute("order", order);
            return "redirect:/checkout/success?code=" + order.getOrderCode();
        } catch (IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("cartItems", cartItems);
            model.addAttribute("cartTotal", cartService.getCartTotal(session));
            return "checkout/index";
        }
    }

    @GetMapping("/success")
    public String orderSuccess(@org.springframework.web.bind.annotation.RequestParam String code, Model model) {
        Order order = orderService.findByOrderCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        model.addAttribute("order", order);
        return "checkout/success";
    }
}
