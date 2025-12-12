package com.sun.expense_management.service.admin;

import com.sun.expense_management.dto.admin.AdminDashboardStats;
import com.sun.expense_management.repository.*;
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
        Long totalUsers = userRepository.count();
        Long activeUsers = userRepository.countByActive(true);
        Long inactiveUsers = totalUsers - activeUsers;

        // New users this month
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        Long newUsersThisMonth = userRepository.countByCreatedAtAfter(startOfMonth);

        // Financial statistics (all users)
        BigDecimal totalExpenses = getAllExpensesSum();
        BigDecimal totalIncomes = getAllIncomesSum();
        BigDecimal totalBalance = totalIncomes.subtract(totalExpenses);

        // Activity statistics
        Long totalCategories = categoryRepository.count();
        Long totalBudgets = budgetRepository.count();
        Long totalExpenseRecords = expenseRepository.count();
        Long totalIncomeRecords = incomeRepository.count();

        // Today's activity
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        Long todayExpenses = countExpensesBetween(startOfDay, endOfDay);
        Long todayIncomes = countIncomesBetween(startOfDay, endOfDay);
        Long todayLogins = countLoginsBetween(startOfDay, endOfDay);

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
        BigDecimal sum = expenseRepository.findAll().stream()
                .map(com.sun.expense_management.entity.Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum != null ? sum : BigDecimal.ZERO;
    }

    private BigDecimal getAllIncomesSum() {
        BigDecimal sum = incomeRepository.findAll().stream()
                .map(com.sun.expense_management.entity.Income::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum != null ? sum : BigDecimal.ZERO;
    }

    private Long countExpensesBetween(LocalDateTime start, LocalDateTime end) {
        return expenseRepository.findAll().stream()
                .filter(e -> e.getCreatedAt().isAfter(start) && e.getCreatedAt().isBefore(end))
                .count();
    }

    private Long countIncomesBetween(LocalDateTime start, LocalDateTime end) {
        return incomeRepository.findAll().stream()
                .filter(i -> i.getCreatedAt().isAfter(start) && i.getCreatedAt().isBefore(end))
                .count();
    }

    private Long countLoginsBetween(LocalDateTime start, LocalDateTime end) {
        // Placeholder - would need ActivityLog repository
        // For now, return 0
        return 0L;
    }
}
