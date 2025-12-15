package com.sunasterisk.expense_management.dto.budget;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetResponse {

    private Long id;
    private String name;
    private BigDecimal amountLimit;
    private BigDecimal spentAmount;
    private BigDecimal remainingAmount;
    private Double usagePercentage;
    private Boolean isOverBudget;
    private Boolean shouldAlert;
    private Integer year;
    private Integer month;
    private String note;
    private Integer alertThreshold;
    private Boolean isAlertSent;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Category info
    private Long categoryId;
    private String categoryName;
    private String categoryIcon;
    private String categoryColor;

    // User info
    private Long userId;
    private String userName;
}
