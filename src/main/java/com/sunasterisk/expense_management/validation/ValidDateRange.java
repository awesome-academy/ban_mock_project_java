package com.sunasterisk.expense_management.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Validation annotation to ensure endDate is after or equal to startDate
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidDateRangeValidator.class)
@Documented
public @interface ValidDateRange {

    String message() default "{report.date.range.invalid}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * Field name for start date
     */
    String startDateField() default "startDate";

    /**
     * Field name for end date
     */
    String endDateField() default "endDate";
}
