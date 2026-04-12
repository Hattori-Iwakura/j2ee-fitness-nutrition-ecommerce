package com.example.j2ee_fitness_nutrition_ecommerce.controller;

import com.example.j2ee_fitness_nutrition_ecommerce.dto.CheckoutRequest;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.Coupon;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.Order;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.Payment;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.User;
import com.example.j2ee_fitness_nutrition_ecommerce.service.*;
import com.example.j2ee_fitness_nutrition_ecommerce.util.SecurityUtils;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    private static final Logger log = LoggerFactory.getLogger(CheckoutController.class);

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
    public String checkoutPage(Authentication authentication,
                               HttpSession session, Model model) {
        String email = SecurityUtils.requireUserEmail(authentication);
        List<CartItem> cartItems = cartService.getCart(session);
        if (cartItems.isEmpty()) {
            return "redirect:/cart";
        }

        CheckoutRequest checkoutRequest = new CheckoutRequest();
        User user = userService.findByEmail(email).orElse(null);
        if (user != null) {
            checkoutRequest.setFullName(user.getFullName());
            checkoutRequest.setPhone(user.getPhone());
            checkoutRequest.setAddress(user.getAddress());
        }

        model.addAttribute("checkoutRequest", checkoutRequest);
        model.addAttribute("cartItems", cartItems);

        BigDecimal cartTotal = cartService.getCartTotal(session);
        model.addAttribute("cartTotal", cartTotal);

        applyCouponFromSessionToModel(session, model, cartTotal);

        return "checkout/index";
    }

    /** Keeps order summary (coupon / total) consistent when redisplaying the checkout form after validation errors. */
    private void applyCouponFromSessionToModel(HttpSession session, Model model, BigDecimal cartTotal) {
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
    }

    @PostMapping
    public String placeOrder(@Valid @ModelAttribute CheckoutRequest checkoutRequest,
                             BindingResult result,
                             Authentication authentication,
                             HttpSession session, Model model,
                             RedirectAttributes redirectAttributes) {
        String email = SecurityUtils.requireUserEmail(authentication);
        List<CartItem> cartItems = cartService.getCart(session);
        if (cartItems.isEmpty()) {
            return "redirect:/cart";
        }

        if (result.hasErrors()) {
            prepareCheckoutForm(model, session, cartItems, checkoutRequest);
            return "checkout/index";
        }

        try {
            String couponCode = (String) session.getAttribute("couponCode");
            Order order = orderService.createOrder(email, checkoutRequest, cartItems, couponCode);
            cartService.clearCart(session);
            session.removeAttribute("couponCode");

            // Redirect based on payment method
            String pm = checkoutRequest.getPaymentMethod();
            if ("BANK_TRANSFER".equals(pm) || "E_WALLET".equals(pm)) {
                return "redirect:/checkout/payment?code=" + order.getOrderCode();
            }
            return "redirect:/checkout/success?code=" + order.getOrderCode();
        } catch (IllegalStateException e) {
            log.warn("placeOrder: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            prepareCheckoutForm(model, session, cartItems, checkoutRequest);
            return "checkout/index";
        } catch (IllegalArgumentException e) {
            log.warn("placeOrder validation: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            prepareCheckoutForm(model, session, cartItems, checkoutRequest);
            return "checkout/index";
        } catch (Exception e) {
            log.error("placeOrder failed", e);
            model.addAttribute("error", "Không thể tạo đơn. Vui lòng thử lại.");
            prepareCheckoutForm(model, session, cartItems, checkoutRequest);
            return "checkout/index";
        }
    }

    private void prepareCheckoutForm(Model model, HttpSession session, List<CartItem> cartItems,
                                     CheckoutRequest checkoutRequest) {
        model.addAttribute("checkoutRequest", checkoutRequest);
        model.addAttribute("cartItems", cartItems);
        BigDecimal cartTotal = cartService.getCartTotal(session);
        model.addAttribute("cartTotal", cartTotal);
        applyCouponFromSessionToModel(session, model, cartTotal);
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
                              Authentication authentication,
                              Model model) {
        Order order = orderService.findByOrderCodeForView(code)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        verifyOwnership(order, SecurityUtils.requireUserEmail(authentication));
        model.addAttribute("order", order);
        model.addAttribute("payment", order.getPayment());
        return "checkout/payment";
    }

    @PostMapping("/payment/confirm")
    public String confirmPayment(@RequestParam String code,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        Order order = orderService.findByOrderCodeForView(code)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        verifyOwnership(order, SecurityUtils.requireUserEmail(authentication));
        Payment payment = order.getPayment();
        if (payment != null) {
            paymentService.confirmPayment(payment.getId());
        }
        return "redirect:/checkout/success?code=" + code;
    }

    @GetMapping("/success")
    public String orderSuccess(@RequestParam String code,
                             Authentication authentication,
                             Model model) {
        Order order = orderService.findByOrderCodeForView(code)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        verifyOwnership(order, SecurityUtils.requireUserEmail(authentication));
        model.addAttribute("order", order);

        // Cross-sell: recommend products based on what was just purchased
        try {
            if (!order.getOrderDetails().isEmpty()) {
                Long firstProductId = order.getOrderDetails().get(0).getVariant().getProduct().getId();
                var crossSell = productService.findCoPurchasedProducts(firstProductId);
                if (crossSell.isEmpty()) {
                    Long categoryId = order.getOrderDetails().get(0).getVariant().getProduct().getCategory().getId();
                    crossSell = productService.findRelatedProducts(firstProductId, categoryId);
                }
                model.addAttribute("crossSellProducts", crossSell);
            }
        } catch (Exception e) {
            log.warn("Cross-sell recommendations skipped for order {}: {}", code, e.getMessage());
            model.addAttribute("crossSellProducts", Collections.emptyList());
        }

        return "checkout/success";
    }

    private void verifyOwnership(Order order, String userEmail) {
        if (!order.getUser().getEmail().equalsIgnoreCase(userEmail)) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied");
        }
    }
}
