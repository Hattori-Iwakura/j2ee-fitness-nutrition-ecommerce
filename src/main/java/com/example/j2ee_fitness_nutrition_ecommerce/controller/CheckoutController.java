package com.example.j2ee_fitness_nutrition_ecommerce.controller;

import com.example.j2ee_fitness_nutrition_ecommerce.dto.CheckoutRequest;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.Coupon;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.Order;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.Payment;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.User;
import com.example.j2ee_fitness_nutrition_ecommerce.enums.PaymentMethod;
import com.example.j2ee_fitness_nutrition_ecommerce.service.*;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    private final CartService cartService;
    private final OrderService orderService;
    private final UserService userService;
    private final CouponService couponService;
    private final PaymentService paymentService;
    private final ProductService productService;

    public CheckoutController(CartService cartService, OrderService orderService,
                              UserService userService, CouponService couponService,
                              PaymentService paymentService, ProductService productService) {
        this.cartService = cartService;
        this.orderService = orderService;
        this.userService = userService;
        this.couponService = couponService;
        this.paymentService = paymentService;
        this.productService = productService;
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

        BigDecimal cartTotal = cartService.getCartTotal(session);
        model.addAttribute("cartTotal", cartTotal);

        // Handle coupon from session
        String couponCode = (String) session.getAttribute("couponCode");
        if (couponCode != null) {
            try {
                Coupon coupon = couponService.validate(couponCode, cartTotal);
                BigDecimal discount = couponService.calculateDiscount(coupon, cartTotal);
                model.addAttribute("couponCode", couponCode);
                model.addAttribute("discount", discount);
                model.addAttribute("finalTotal", cartTotal.subtract(discount));
            } catch (IllegalArgumentException e) {
                session.removeAttribute("couponCode");
            }
        }

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
            String couponCode = (String) session.getAttribute("couponCode");
            Order order = orderService.createOrder(userDetails.getUsername(), checkoutRequest, cartItems, couponCode);
            cartService.clearCart(session);
            session.removeAttribute("couponCode");

            // Redirect based on payment method
            String pm = checkoutRequest.getPaymentMethod();
            if ("BANK_TRANSFER".equals(pm) || "E_WALLET".equals(pm)) {
                return "redirect:/checkout/payment?code=" + order.getOrderCode();
            }
            return "redirect:/checkout/success?code=" + order.getOrderCode();
        } catch (IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("cartItems", cartItems);
            model.addAttribute("cartTotal", cartService.getCartTotal(session));
            return "checkout/index";
        }
    }

    @PostMapping("/apply-coupon")
    public String applyCoupon(@RequestParam String couponCode,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        try {
            BigDecimal cartTotal = cartService.getCartTotal(session);
            couponService.validate(couponCode, cartTotal);
            session.setAttribute("couponCode", couponCode);
            redirectAttributes.addFlashAttribute("success", "Coupon applied successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("couponError", e.getMessage());
        }
        return "redirect:/checkout";
    }

    @PostMapping("/remove-coupon")
    public String removeCoupon(HttpSession session, RedirectAttributes redirectAttributes) {
        session.removeAttribute("couponCode");
        redirectAttributes.addFlashAttribute("success", "Coupon removed.");
        return "redirect:/checkout";
    }

    @GetMapping("/payment")
    public String paymentPage(@RequestParam String code,
                              @AuthenticationPrincipal UserDetails userDetails,
                              Model model) {
        Order order = orderService.findByOrderCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        verifyOwnership(order, userDetails);
        model.addAttribute("order", order);
        model.addAttribute("payment", order.getPayment());
        return "checkout/payment";
    }

    @PostMapping("/payment/confirm")
    public String confirmPayment(@RequestParam String code,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        Order order = orderService.findByOrderCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        verifyOwnership(order, userDetails);
        Payment payment = order.getPayment();
        if (payment != null) {
            paymentService.confirmPayment(payment.getId());
        }
        return "redirect:/checkout/success?code=" + code;
    }

    @GetMapping("/success")
    public String orderSuccess(@RequestParam String code,
                               @AuthenticationPrincipal UserDetails userDetails,
                               Model model) {
        Order order = orderService.findByOrderCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        verifyOwnership(order, userDetails);
        model.addAttribute("order", order);

        // Cross-sell: recommend products based on what was just purchased
        if (!order.getOrderDetails().isEmpty()) {
            Long firstProductId = order.getOrderDetails().get(0).getVariant().getProduct().getId();
            var crossSell = productService.findCoPurchasedProducts(firstProductId);
            if (crossSell.isEmpty()) {
                Long categoryId = order.getOrderDetails().get(0).getVariant().getProduct().getCategory().getId();
                crossSell = productService.findRelatedProducts(firstProductId, categoryId);
            }
            model.addAttribute("crossSellProducts", crossSell);
        }

        return "checkout/success";
    }

    private void verifyOwnership(Order order, UserDetails userDetails) {
        if (!order.getUser().getEmail().equals(userDetails.getUsername())) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied");
        }
    }
}
