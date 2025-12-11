package com.sun.expense_management.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Validation annotation to ensure sortDir field contains only "asc" or "desc"
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidSortDirectionValidator.class)
@Documented
public @interface ValidSortDirection {

    String message() default "{validation.sort.direction.invalid}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
