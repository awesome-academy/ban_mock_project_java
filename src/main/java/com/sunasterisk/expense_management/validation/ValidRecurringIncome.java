package com.sunasterisk.expense_management.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = RecurringIncomeValidator.class)
@Documented
public @interface ValidRecurringIncome {
    String message() default "{income.recurring.type.required}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
