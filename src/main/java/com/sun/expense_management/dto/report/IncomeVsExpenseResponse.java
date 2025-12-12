package com.sun.expense_management.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Response for income vs expense comparison
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncomeVsExpenseResponse {

    private LocalDate startDate;
    private LocalDate endDate;

    // Income data
    private BigDecimal totalIncome;
    private Long incomeCount;
    private BigDecimal averageIncome;

    // Expense data
    private BigDecimal totalExpense;
    private Long expenseCount;
    private BigDecimal averageExpense;

    // Comparison
    private BigDecimal balance; // income - expense
    private BigDecimal savingsRate; // (income - expense) / income * 100
    private String financialHealth; // "SURPLUS", "DEFICIT", "BALANCED"
}
