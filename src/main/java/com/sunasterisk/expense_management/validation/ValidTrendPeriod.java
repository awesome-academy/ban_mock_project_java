package com.sunasterisk.expense_management.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Validation annotation to ensure period value is valid for trend analysis
 */
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidTrendPeriodValidator.class)
@Documented
public @interface ValidTrendPeriod {

    String message() default "{report.invalid.period}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
