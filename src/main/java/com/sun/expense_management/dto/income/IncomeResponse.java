package com.sun.expense_management.dto.income;

import com.sun.expense_management.entity.Income.RecurringType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class IncomeResponse {

    private Long id;
    private String name;
    private BigDecimal amount;
    private LocalDate incomeDate;
    private String note;
    private String source;
    private Boolean isRecurring;
    private RecurringType recurringType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Long categoryId;
    private String categoryName;
    private String categoryIcon;
}
