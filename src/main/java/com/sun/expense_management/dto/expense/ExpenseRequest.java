package com.sun.expense_management.dto.expense;

import com.sun.expense_management.entity.Expense.PaymentMethod;
import com.sun.expense_management.entity.Expense.RecurringType;
import com.sun.expense_management.validation.ValidRecurringExpense;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@ValidRecurringExpense
public class ExpenseRequest {

    @NotBlank(message = "{expense.name.required}")
    @Size(max = 200, message = "{expense.name.max.length}")
    private String name;

    @NotNull(message = "{expense.amount.required}")
    @Positive(message = "{expense.amount.positive}")
    @DecimalMax(value = "9999999999999.99", message = "{expense.amount.max}")
    private BigDecimal amount;

    @NotNull(message = "{expense.date.required}")
    @PastOrPresent(message = "{expense.date.past.or.present}")
    private LocalDate expenseDate;

    @NotNull(message = "{expense.category.required}")
    private Long categoryId;

    @Size(max = 1000, message = "{expense.note.max.length}")
    private String note;

    @Size(max = 100, message = "{expense.location.max.length}")
    private String location;

    @NotNull(message = "{expense.payment.method.required}")
    private PaymentMethod paymentMethod = PaymentMethod.CASH;

    @NotNull(message = "{expense.is.recurring.required}")
    private Boolean isRecurring = false;

    private RecurringType recurringType;
}
