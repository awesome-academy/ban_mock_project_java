package com.sun.expense_management.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator implementation for @ValidSortDirection annotation
 * Validates that sortDir field contains only "asc" or "desc" (case-insensitive)
 */
public class ValidSortDirectionValidator implements ConstraintValidator<ValidSortDirection, String> {

    @Override
    public boolean isValid(String sortDir, ConstraintValidatorContext context) {
        // Null or empty is valid (will use default)
        if (sortDir == null || sortDir.trim().isEmpty()) {
            return true;
        }

        String normalized = sortDir.trim().toLowerCase();

        if (!normalized.equals("asc") && !normalized.equals("desc")) {
            // Build custom error message with I18n
            context.disableDefaultConstraintViolation();
            String errorMessage = ValidatorContextHelper.getMessage(
                    "validation.sort.direction.invalid",
                    sortDir
            );
            context.buildConstraintViolationWithTemplate(errorMessage)
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
