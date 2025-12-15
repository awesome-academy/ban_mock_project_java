package com.sunasterisk.expense_management.validation;

import com.sunasterisk.expense_management.dto.income.IncomeRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RecurringIncomeValidator implements ConstraintValidator<ValidRecurringIncome, IncomeRequest> {

    @Override
    public boolean isValid(IncomeRequest request, ConstraintValidatorContext context) {
        if (request == null) {
            return true;
        }

        // If isRecurring is true, recurringType must not be null
        if (Boolean.TRUE.equals(request.getIsRecurring()) && request.getRecurringType() == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("{income.recurring.type.required}")
                    .addPropertyNode("recurringType")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
