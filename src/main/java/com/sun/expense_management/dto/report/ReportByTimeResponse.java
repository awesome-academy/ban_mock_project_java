package com.sun.expense_management.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Response for expense report by time period
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportByTimeResponse {

    private LocalDate startDate;
    private LocalDate endDate;

    private BigDecimal totalExpense;
    private BigDecimal totalIncome;
    private BigDecimal balance; // income - expense

    private Long expenseCount;
    private Long incomeCount;

    private BigDecimal averageExpense; // total / count
    private BigDecimal averageIncome;

    private String period; // "month", "quarter", "year", "custom"
}
