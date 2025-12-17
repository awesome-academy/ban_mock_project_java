package com.sunasterisk.expense_management.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for admin dashboard statistics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardStats {

    // User Statistics
    private Long totalUsers;
    private Long activeUsers;
    private Long inactiveUsers;
    private Long newUsersThisMonth;

    // Financial Statistics
    private BigDecimal totalExpenses;
    private BigDecimal totalIncomes;
    private BigDecimal totalBalance;

    // Activity Statistics
    private Long totalCategories;
    private Long totalBudgets;
    private Long totalExpenseRecords;
    private Long totalIncomeRecords;

    // Recent Activity
    private Long todayExpenses;
    private Long todayIncomes;
    private Long todayLogins;
}
