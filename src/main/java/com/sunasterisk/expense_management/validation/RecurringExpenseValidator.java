package com.sunasterisk.expense_management.validation;

import com.sunasterisk.expense_management.dto.expense.ExpenseRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator implementation for @ValidRecurringExpense annotation
 * Validates the relationship between isRecurring and recurringType fields
 */
public class RecurringExpenseValidator implements ConstraintValidator<ValidRecurringExpense, ExpenseRequest> {

    @Override
    public void initialize(ValidRecurringExpense constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(ExpenseRequest request, ConstraintValidatorContext context) {
        if (request == null) {
            return true; // null objects are validated by @NotNull
        }

        // If isRecurring is null, let @NotNull handle it
        if (request.getIsRecurring() == null) {
            return true;
        }

        // If isRecurring is true, recurringType must not be null
        if (Boolean.TRUE.equals(request.getIsRecurring())) {
            if (request.getRecurringType() == null) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("{expense.recurring.type.required}")
                        .addPropertyNode("recurringType")
                        .addConstraintViolation();
                return false;
            }
        }

        // All validations passed
        return true;
    }
}
