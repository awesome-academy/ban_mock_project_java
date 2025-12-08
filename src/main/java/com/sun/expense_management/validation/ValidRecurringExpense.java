package com.sun.expense_management.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Custom validation annotation to ensure recurring expense logic is correct:
 * - If isRecurring is true, recurringType must not be null
 * - If isRecurring is false, recurringType should be ignored (can be null)
 */
@Documented
@Constraint(validatedBy = RecurringExpenseValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidRecurringExpense {
    String message() default "Khi chi tiêu định kỳ, loại định kỳ không được để trống";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
