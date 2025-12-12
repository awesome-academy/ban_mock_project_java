package com.sun.expense_management.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response for trend analysis over time
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrendAnalysisResponse {

    private String period; // "MONTHLY", "QUARTERLY", "YEARLY"
    private List<TrendItem> trends;

    // Summary statistics
    private BigDecimal averageExpense;
    private BigDecimal maxExpense;
    private BigDecimal minExpense;
    private String trendDirection; // "INCREASING", "DECREASING", "STABLE"

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TrendItem {
        private String period; // "2025-12", "2025-Q4", "2025"
        private Integer year;
        private Integer month; // nullable for quarter/year
        private Integer quarter; // nullable for month/year

        private BigDecimal totalExpense;
        private BigDecimal totalIncome;
        private BigDecimal balance;

        private Long expenseCount;
        private Long incomeCount;

        private Double changePercentage; // % change from previous period
    }
}
