package com.sun.expense_management.dto.budget;

import com.sun.expense_management.entity.Budget;
import com.sun.expense_management.validation.ValidSortDirection;
import com.sun.expense_management.validation.ValidSortFields;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetFilterRequest {

    private String name;
    private Long categoryId;
    private Integer year;
    private Integer month;
    private Boolean isOverBudget;
    private Boolean active;

    @Builder.Default
    private Integer page = 0;

    @Builder.Default
    private Integer size = 10;

    @Builder.Default
    @ValidSortFields(
        entityClass = Budget.class,
        allowedFields = {"id", "name", "amountLimit", "spentAmount", "year", "month", "alertThreshold", "active", "createdAt", "updatedAt"},
        message = "{validation.budget.sort.fields.invalid}"
    )
    private String sortBy = "year,month";

    @Builder.Default
    @ValidSortDirection
    private String sortDir = "desc";
}
