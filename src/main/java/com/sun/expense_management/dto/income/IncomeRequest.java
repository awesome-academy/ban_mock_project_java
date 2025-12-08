package com.sun.expense_management.dto.income;

import com.sun.expense_management.entity.Income.RecurringType;
import com.sun.expense_management.validation.ValidRecurringIncome;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@ValidRecurringIncome
public class IncomeRequest {

    @NotBlank(message = "{income.name.required}")
    @Size(max = 200, message = "{income.name.max.length}")
    private String name;

    @NotNull(message = "{income.amount.required}")
    @Positive(message = "{income.amount.positive}")
    @DecimalMax(value = "9999999999999.99", message = "{income.amount.max}")
    private BigDecimal amount;

    @NotNull(message = "{income.date.required}")
    @PastOrPresent(message = "{income.date.past.or.present}")
    private LocalDate incomeDate;

    @NotNull(message = "{income.category.required}")
    private Long categoryId;

    @Size(max = 1000, message = "{income.note.max.length}")
    private String note;

    @Size(max = 100, message = "{income.source.max.length}")
    private String source;

    @NotNull(message = "{income.is.recurring.required}")
    private Boolean isRecurring = false;

    private RecurringType recurringType;
}
