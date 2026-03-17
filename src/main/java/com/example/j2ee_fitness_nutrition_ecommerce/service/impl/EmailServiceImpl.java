package com.example.j2ee_fitness_nutrition_ecommerce.service.impl;

import com.example.j2ee_fitness_nutrition_ecommerce.entity.Order;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.User;
import com.example.j2ee_fitness_nutrition_ecommerce.enums.OrderStatus;
import com.example.j2ee_fitness_nutrition_ecommerce.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.mail.from:noreply@fitshop.com}")
    private String fromEmail;

    public EmailServiceImpl(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    @Override
    @Async
    public void sendOrderConfirmation(Order order) {
        try {
            Context context = new Context();
            context.setVariable("order", order);
            String html = templateEngine.process("email/order-confirmation", context);
            sendHtmlEmail(order.getUser().getEmail(), "Order Confirmation - " + order.getOrderCode(), html);
        } catch (Exception e) {
            log.error("Failed to send order confirmation email for order {}: {}", order.getOrderCode(), e.getMessage());
        }
    }

    @Override
    @Async
    public void sendOrderStatusUpdate(Order order, OrderStatus newStatus) {
        try {
            Context context = new Context();
            context.setVariable("order", order);
            context.setVariable("newStatus", newStatus);
            String html = templateEngine.process("email/order-status-update", context);
            sendHtmlEmail(order.getUser().getEmail(), "Order Update - " + order.getOrderCode(), html);
        } catch (Exception e) {
            log.error("Failed to send order status update email for order {}: {}", order.getOrderCode(), e.getMessage());
        }
    }

    @Override
    @Async
    public void sendWelcomeEmail(User user) {
        try {
            Context context = new Context();
            context.setVariable("user", user);
            String html = templateEngine.process("email/welcome", context);
            sendHtmlEmail(user.getEmail(), "Welcome to FitShop!", html);
        } catch (Exception e) {
            log.error("Failed to send welcome email to {}: {}", user.getEmail(), e.getMessage());
        }
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        mailSender.send(message);
        log.info("Email sent to {} with subject: {}", to, subject);
    }
}
