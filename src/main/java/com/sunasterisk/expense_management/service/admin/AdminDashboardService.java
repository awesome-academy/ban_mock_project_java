package com.sunasterisk.expense_management.service.admin;

import com.sunasterisk.expense_management.dto.admin.AdminDashboardStats;
import com.sunasterisk.expense_management.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Service for admin dashboard operations
 */
@Service
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;
    private final IncomeRepository incomeRepository;
    private final CategoryRepository categoryRepository;
    private final BudgetRepository budgetRepository;

    public AdminDashboardService(UserRepository userRepository,
                                ExpenseRepository expenseRepository,
                                IncomeRepository incomeRepository,
                                CategoryRepository categoryRepository,
                                BudgetRepository budgetRepository) {
        this.userRepository = userRepository;
        this.expenseRepository = expenseRepository;
        this.incomeRepository = incomeRepository;
        this.categoryRepository = categoryRepository;
        this.budgetRepository = budgetRepository;
    }

    /**
     * Get dashboard statistics
     */
    @Transactional(readOnly = true)
    public AdminDashboardStats getDashboardStatistics() {
        // User statistics
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByActive(true);
        long inactiveUsers = totalUsers - activeUsers;

        // New users this month
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        long newUsersThisMonth = userRepository.countByCreatedAtAfter(startOfMonth);
        // Financial statistics (all users)
        BigDecimal totalExpenses = getAllExpensesSum();
        BigDecimal totalIncomes = getAllIncomesSum();
        BigDecimal totalBalance = totalIncomes.subtract(totalExpenses);

        // Activity statistics
        long totalCategories = categoryRepository.count();
        long totalBudgets = budgetRepository.count();
        long totalExpenseRecords = expenseRepository.count();
        long totalIncomeRecords = incomeRepository.count();

        // Today's activity
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        long todayExpenses = countExpensesBetween(startOfDay, endOfDay);
        long todayIncomes = countIncomesBetween(startOfDay, endOfDay);
        long todayLogins = countLoginsBetween(startOfDay, endOfDay);

        return AdminDashboardStats.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .inactiveUsers(inactiveUsers)
                .newUsersThisMonth(newUsersThisMonth)
                .totalExpenses(totalExpenses)
                .totalIncomes(totalIncomes)
                .totalBalance(totalBalance)
                .totalCategories(totalCategories)
                .totalBudgets(totalBudgets)
                .totalExpenseRecords(totalExpenseRecords)
                .totalIncomeRecords(totalIncomeRecords)
                .todayExpenses(todayExpenses)
                .todayIncomes(todayIncomes)
                .todayLogins(todayLogins)
                .build();
    }

    private BigDecimal getAllExpensesSum() {
        return expenseRepository.sumAllExpenses().orElse(BigDecimal.ZERO);
    }

    private BigDecimal getAllIncomesSum() {
        return incomeRepository.sumAllIncomes().orElse(BigDecimal.ZERO);
    }

    private Long countExpensesBetween(LocalDateTime start, LocalDateTime end) {
        return expenseRepository.countExpensesBetween(start, end);
    }

    private Long countIncomesBetween(LocalDateTime start, LocalDateTime end) {
        return incomeRepository.countIncomesBetween(start, end);
    }

    private Long countLoginsBetween(LocalDateTime start, LocalDateTime end) {
        // Placeholder - would need ActivityLog repository
        // For now, return 0
        return 0L;
    }
}
