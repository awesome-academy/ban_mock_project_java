package com.sunasterisk.expense_management.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Validator implementation for @ValidTrendPeriod annotation
 * Validates that period value is one of: MONTHLY, QUARTERLY, YEARLY
 */
public class ValidTrendPeriodValidator implements ConstraintValidator<ValidTrendPeriod, String> {

    private static final Set<String> VALID_PERIODS = new HashSet<>(
            Arrays.asList("MONTHLY", "QUARTERLY", "YEARLY")
    );

    @Override
    public boolean isValid(String period, ConstraintValidatorContext context) {
        // Null is valid (let @NotNull handle it if required)
        if (period == null || period.trim().isEmpty()) {
            return true;
        }

        String normalizedPeriod = period.trim().toUpperCase();

        if (!VALID_PERIODS.contains(normalizedPeriod)) {
            // Build custom error message with I18n
            context.disableDefaultConstraintViolation();
            String errorMessage = ValidatorContextHelper.getMessage(
                    "validation.period.invalid",
                    period,
                    String.join(", ", VALID_PERIODS)
            );
            context.buildConstraintViolationWithTemplate(errorMessage)
                    .addConstraintViolation();
            return false;
        }        return true;
    }
}
