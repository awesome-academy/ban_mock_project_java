package com.sun.expense_management.dto.income;

import com.sun.expense_management.entity.Income;
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

    public static IncomeResponse fromEntity(Income income) {
        return IncomeResponse.builder()
                .id(income.getId())
                .name(income.getName())
                .amount(income.getAmount())
                .incomeDate(income.getIncomeDate())
                .note(income.getNote())
                .source(income.getSource())
                .isRecurring(income.getIsRecurring())
                .recurringType(income.getRecurringType())
                .createdAt(income.getCreatedAt())
                .updatedAt(income.getUpdatedAt())
                .categoryId(income.getCategory() != null ? income.getCategory().getId() : null)
                .categoryName(income.getCategory() != null ? income.getCategory().getName() : null)
                .categoryIcon(income.getCategory() != null ? income.getCategory().getIcon() : null)
                .build();
    }
}
