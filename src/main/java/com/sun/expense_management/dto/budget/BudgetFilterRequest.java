package com.sun.expense_management.dto.budget;

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
    private String sortBy = "year,month";

    @Builder.Default
    private String sortDir = "desc";
}
