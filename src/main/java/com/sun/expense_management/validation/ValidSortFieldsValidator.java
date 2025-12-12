package com.sun.expense_management.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Validator implementation for @ValidSortFields annotation
 * Validates that sortBy field contains only valid entity field names
 */
public class ValidSortFieldsValidator implements ConstraintValidator<ValidSortFields, String> {

    private Set<String> allowedFields;

    @Override
    public void initialize(ValidSortFields constraintAnnotation) {
        // If custom allowed fields are provided, use them
        if (constraintAnnotation.allowedFields().length > 0) {
            this.allowedFields = new HashSet<>(Arrays.asList(constraintAnnotation.allowedFields()));
        } else {
            // Otherwise, extract field names from entity class
            Class<?> entityClass = constraintAnnotation.entityClass();
            this.allowedFields = Arrays.stream(entityClass.getDeclaredFields())
                    .map(Field::getName)
                    .collect(Collectors.toSet());
        }
    }

    @Override
    public boolean isValid(String sortBy, ConstraintValidatorContext context) {
        // Null or empty is valid (will use default)
        if (sortBy == null || sortBy.trim().isEmpty()) {
            return true;
        }

        // Split by comma and validate each field
        String[] fields = sortBy.split(",");
        for (String field : fields) {
            String trimmedField = field.trim();
            if (!allowedFields.contains(trimmedField)) {
                // Build custom error message with I18n
                context.disableDefaultConstraintViolation();
                String errorMessage = ValidatorContextHelper.getMessage(
                        "validation.sort.fields.invalid",
                        trimmedField,
                        String.join(", ", allowedFields)
                );
                context.buildConstraintViolationWithTemplate(errorMessage)
                        .addConstraintViolation();
                return false;
            }
        }

        return true;
    }
}
