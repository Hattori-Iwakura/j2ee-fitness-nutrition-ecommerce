package com.example.j2ee_fitness_nutrition_ecommerce.controller.admin;

import com.example.j2ee_fitness_nutrition_ecommerce.service.StockLogService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/stock-logs")
public class AdminStockLogController {

    private final StockLogService stockLogService;

    public AdminStockLogController(StockLogService stockLogService) {
        this.stockLogService = stockLogService;
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page, Model model) {
        Pageable pageable = PageRequest.of(page, 40, Sort.by(Sort.Direction.DESC, "createdAt"));
        model.addAttribute("logs", stockLogService.findAll(pageable));
        return "admin/stock-log/list";
    }
}
