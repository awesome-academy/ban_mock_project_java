package com.sun.expense_management.dto.budget;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetRequest {

    @NotBlank(message = "{budget.name.required}")
    @Size(max = 200, message = "{budget.name.max.length}")
    private String name;

    @NotNull(message = "{budget.amount.required}")
    @DecimalMin(value = "0.01", message = "{budget.amount.positive}")
    private BigDecimal amountLimit;

    @NotNull(message = "{budget.year.required}")
    @Min(value = 2000, message = "{validation.min.value}")
    @Max(value = 2100, message = "{validation.max.value}")
    private Integer year;

    @NotNull(message = "{budget.month.required}")
    @Min(value = 1, message = "{validation.min.value}")
    @Max(value = 12, message = "{validation.max.value}")
    private Integer month;

    private Long categoryId;

    @Size(max = 1000, message = "{validation.max.length}")
    private String note;

    @Min(value = 0, message = "{validation.min.value}")
    @Max(value = 100, message = "{validation.max.value}")
    @Builder.Default
    private Integer alertThreshold = 80;

    @Builder.Default
    private Boolean active = true;
}
