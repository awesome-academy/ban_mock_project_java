package com.sunasterisk.expense_management.dto.income;

import com.sunasterisk.expense_management.entity.Income.RecurringType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
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

    private Long userId;
    private String userName;
    private String userEmail;

    private Long categoryId;
    private String categoryName;
    private String categoryIcon;
}
