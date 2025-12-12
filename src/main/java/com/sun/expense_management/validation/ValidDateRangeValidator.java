package com.sun.expense_management.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Validator implementation for @ValidDateRange annotation
 * Validates that end date is after or equal to start date
 */
public class ValidDateRangeValidator implements ConstraintValidator<ValidDateRange, Object> {

    private String startDateField;
    private String endDateField;

    @Override
    public void initialize(ValidDateRange constraintAnnotation) {
        this.startDateField = constraintAnnotation.startDateField();
        this.endDateField = constraintAnnotation.endDateField();
    }

    @Override
    public boolean isValid(Object object, ConstraintValidatorContext context) {
        try {
            // Get field values using reflection
            Field startField = object.getClass().getDeclaredField(startDateField);
            Field endField = object.getClass().getDeclaredField(endDateField);

            startField.setAccessible(true);
            endField.setAccessible(true);

            Object startValue = startField.get(object);
            Object endValue = endField.get(object);

            // If either is null, let @NotNull handle it
            if (startValue == null || endValue == null) {
                return true;
            }

            // Compare dates based on type
            if (startValue instanceof LocalDate && endValue instanceof LocalDate) {
                LocalDate startDate = (LocalDate) startValue;
                LocalDate endDate = (LocalDate) endValue;

                if (endDate.isBefore(startDate)) {
                    // Build custom error message with I18n
                    context.disableDefaultConstraintViolation();
                    String errorMessage = ValidatorContextHelper.getMessage("validation.date.range.invalid");
                    context.buildConstraintViolationWithTemplate(errorMessage)
                            .addPropertyNode(endDateField)
                            .addConstraintViolation();
                    return false;
                }
            } else if (startValue instanceof LocalDateTime && endValue instanceof LocalDateTime) {
                LocalDateTime startDateTime = (LocalDateTime) startValue;
                LocalDateTime endDateTime = (LocalDateTime) endValue;

                if (endDateTime.isBefore(startDateTime)) {
                    // Build custom error message with I18n
                    context.disableDefaultConstraintViolation();
                    String errorMessage = ValidatorContextHelper.getMessage("validation.date.range.invalid");
                    context.buildConstraintViolationWithTemplate(errorMessage)
                            .addPropertyNode(endDateField)
                            .addConstraintViolation();
                    return false;
                }
            }

            return true;

        } catch (NoSuchFieldException | IllegalAccessException e) {
            // If fields don't exist or can't be accessed, validation fails
            return false;
        }
    }
}
