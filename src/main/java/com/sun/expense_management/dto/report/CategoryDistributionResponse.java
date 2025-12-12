package com.sun.expense_management.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Response for expense distribution by category
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDistributionResponse {

    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalExpense;

    private List<CategoryItem> categories;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategoryItem {
        private Long categoryId;
        private String categoryName;
        private String categoryIcon;
        private String categoryColor;

        private BigDecimal amount;
        private Long count;
        private Double percentage; // % of total expense
    }
}
