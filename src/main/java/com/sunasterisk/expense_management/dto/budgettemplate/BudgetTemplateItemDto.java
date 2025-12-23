package com.sunasterisk.expense_management.dto.budgettemplate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetTemplateItemDto {

    private Long id;

    @NotNull(message = "{budget.template.item.category.required}")
    private Long categoryId;

    private String categoryName;

    @NotNull(message = "{budget.amount.required}")
    @Positive(message = "{budget.amount.positive}")
    private BigDecimal defaultAmount;
}
