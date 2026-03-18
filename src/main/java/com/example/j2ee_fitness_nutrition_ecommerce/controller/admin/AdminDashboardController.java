package com.example.j2ee_fitness_nutrition_ecommerce.controller.admin;

import com.example.j2ee_fitness_nutrition_ecommerce.service.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    private final DashboardService dashboardService;

    public AdminDashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("totalOrders", dashboardService.getTotalOrders());
        model.addAttribute("totalProducts", dashboardService.getTotalProducts());
        model.addAttribute("totalUsers", dashboardService.getTotalUsers());
        model.addAttribute("totalRevenue", dashboardService.getTotalRevenue());
        model.addAttribute("orderCountsByStatus", dashboardService.getOrderCountsByStatus());
        model.addAttribute("recentOrders", dashboardService.getRecentOrders());
        model.addAttribute("topProducts", dashboardService.getTopSellingProducts());
        model.addAttribute("lowStockVariants", dashboardService.getLowStockVariants());
        model.addAttribute("monthlyStats", dashboardService.getMonthlyStats());
        model.addAttribute("newUsersThisMonth", dashboardService.getNewUsersThisMonth());
        return "admin/dashboard/index";
    }
}
