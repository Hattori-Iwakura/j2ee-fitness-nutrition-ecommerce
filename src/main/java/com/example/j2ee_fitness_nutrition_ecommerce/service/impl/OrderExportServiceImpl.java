package com.example.j2ee_fitness_nutrition_ecommerce.service.impl;

import com.example.j2ee_fitness_nutrition_ecommerce.entity.Order;
import com.example.j2ee_fitness_nutrition_ecommerce.entity.OrderDetail;
import com.example.j2ee_fitness_nutrition_ecommerce.service.OrderExportService;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class OrderExportServiceImpl implements OrderExportService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public byte[] exportOrdersToCsv(List<Order> orders) {
        StringBuilder sb = new StringBuilder();

        // BOM for Excel UTF-8 compatibility
        sb.append('\uFEFF');

        // Header row
        sb.append("Order Code,Customer,Email,Phone,Address,Status,")
          .append("Product,Flavor,Weight,Qty,Unit Price (VND),Subtotal (VND),")
          .append("Discount (VND),Order Total (VND),Date\n");

        for (Order order : orders) {
            String orderCode = escapeCsv(order.getOrderCode());
            String customer = escapeCsv(order.getFullName());
            String email = escapeCsv(order.getUser() != null ? order.getUser().getEmail() : "");
            String phone = escapeCsv(order.getPhone());
            String address = escapeCsv(order.getAddress());
            String status = order.getStatus().name();
            String discount = order.getDiscountAmount() != null ? order.getDiscountAmount().toPlainString() : "0";
            String total = order.getTotalAmount().toPlainString();
            String date = order.getCreatedAt() != null ? order.getCreatedAt().format(DATE_FMT) : "";

            if (order.getOrderDetails() == null || order.getOrderDetails().isEmpty()) {
                // Order with no details (edge case)
                sb.append(orderCode).append(',')
                  .append(customer).append(',')
                  .append(email).append(',')
                  .append(phone).append(',')
                  .append(address).append(',')
                  .append(status).append(',')
                  .append(",,,,,")
                  .append(discount).append(',')
                  .append(total).append(',')
                  .append(date).append('\n');
            } else {
                boolean firstLine = true;
                for (OrderDetail detail : order.getOrderDetails()) {
                    if (firstLine) {
                        sb.append(orderCode).append(',')
                          .append(customer).append(',')
                          .append(email).append(',')
                          .append(phone).append(',')
                          .append(address).append(',')
                          .append(status).append(',');
                    } else {
                        // Repeat order-level fields blank for subsequent item rows
                        sb.append(",,,,,,");
                    }

                    String productName = "";
                    String flavor = "";
                    String weight = "";
                    if (detail.getVariant() != null) {
                        flavor = escapeCsv(detail.getVariant().getFlavor());
                        weight = escapeCsv(detail.getVariant().getWeight());
                        if (detail.getVariant().getProduct() != null) {
                            productName = escapeCsv(detail.getVariant().getProduct().getName());
                        }
                    }

                    sb.append(productName).append(',')
                      .append(flavor).append(',')
                      .append(weight).append(',')
                      .append(detail.getQuantity()).append(',')
                      .append(detail.getUnitPrice().toPlainString()).append(',')
                      .append(detail.getSubtotal().toPlainString()).append(',');

                    if (firstLine) {
                        sb.append(discount).append(',')
                          .append(total).append(',')
                          .append(date);
                        firstLine = false;
                    } else {
                        sb.append(",,");
                    }
                    sb.append('\n');
                }
            }
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
