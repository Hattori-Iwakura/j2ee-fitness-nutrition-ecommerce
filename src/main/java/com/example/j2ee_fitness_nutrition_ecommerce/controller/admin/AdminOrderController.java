package com.example.j2ee_fitness_nutrition_ecommerce.controller.admin;

import com.example.j2ee_fitness_nutrition_ecommerce.entity.Order;
import com.example.j2ee_fitness_nutrition_ecommerce.enums.OrderStatus;
import com.example.j2ee_fitness_nutrition_ecommerce.service.EmailService;
import com.example.j2ee_fitness_nutrition_ecommerce.service.OrderExportService;
import com.example.j2ee_fitness_nutrition_ecommerce.service.OrderService;
import com.example.j2ee_fitness_nutrition_ecommerce.service.PaymentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/admin/orders")
public class AdminOrderController {

    private final OrderService orderService;
    private final PaymentService paymentService;
    private final EmailService emailService;
    private final OrderExportService orderExportService;

    public AdminOrderController(OrderService orderService, PaymentService paymentService,
                                EmailService emailService, OrderExportService orderExportService) {
        this.orderService = orderService;
        this.paymentService = paymentService;
        this.emailService = emailService;
        this.orderExportService = orderExportService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) OrderStatus status,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       Model model) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Order> orders;
        if (status != null) {
            orders = orderService.findByStatus(status, pageable);
        } else {
            orders = orderService.findAll(pageable);
        }

        model.addAttribute("orders", orders);
        model.addAttribute("currentStatus", status);
        model.addAttribute("statuses", OrderStatus.values());
        return "admin/order/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Order order = orderService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        model.addAttribute("order", order);
        model.addAttribute("statuses", OrderStatus.values());
        return "admin/order/detail";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam OrderStatus status,
                               RedirectAttributes redirectAttributes) {
        Order order = orderService.updateStatus(id, status);
        emailService.sendOrderStatusUpdate(order, status);
        redirectAttributes.addFlashAttribute("success", "Order status updated!");
        return "redirect:/admin/orders/" + id;
    }

    @PostMapping("/{id}/payment/confirm")
    public String confirmPayment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Order order = orderService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        if (order.getPayment() != null) {
            paymentService.confirmPayment(order.getPayment().getId());
        }
        redirectAttributes.addFlashAttribute("success", "Payment confirmed!");
        return "redirect:/admin/orders/" + id;
    }

    @PostMapping("/{id}/payment/fail")
    public String failPayment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Order order = orderService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        if (order.getPayment() != null) {
            paymentService.failPayment(order.getPayment().getId());
        }
        redirectAttributes.addFlashAttribute("success", "Payment marked as failed.");
        return "redirect:/admin/orders/" + id;
    }

    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportCsv() {
        List<Order> allOrders = orderService.findAll();
        byte[] csvBytes = orderExportService.exportOrdersToCsv(allOrders);

        String filename = "orders-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csvBytes);
    }
}

