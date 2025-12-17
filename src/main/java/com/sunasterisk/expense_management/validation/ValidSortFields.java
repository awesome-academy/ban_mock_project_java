package com.sunasterisk.expense_management.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Validation annotation to ensure sortBy field contains only valid entity field names
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidSortFieldsValidator.class)
@Documented
public @interface ValidSortFields {

    String message() default "{validation.sort.fields.invalid}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * The entity class to validate field names against
     */
    Class<?> entityClass();

    /**
     * Optional custom allowed field names (if not using entity fields)
     */
    String[] allowedFields() default {};
}
