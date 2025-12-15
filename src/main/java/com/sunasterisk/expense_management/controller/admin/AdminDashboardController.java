package com.sunasterisk.expense_management.controller.admin;

import com.sunasterisk.expense_management.dto.admin.AdminDashboardStats;
import com.sunasterisk.expense_management.service.admin.AdminDashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller for admin dashboard
 */
@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    private final AdminDashboardService dashboardService;

    public AdminDashboardController(AdminDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * Show admin dashboard
     */
    @GetMapping({"/", "/dashboard"})
    public String showDashboard(Model model) {
        AdminDashboardStats stats = dashboardService.getDashboardStatistics();

        // Add stats object to model
        model.addAttribute("stats", stats);
        model.addAttribute("activeMenu", "dashboard");
        return "admin/dashboard";
    }
}
