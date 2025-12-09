package com.sun.expense_management.dto.expense;

import com.sun.expense_management.entity.Expense.PaymentMethod;
import com.sun.expense_management.entity.Expense.RecurringType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class ExpenseResponse {

    private Long id;
    private String name;
    private BigDecimal amount;
    private LocalDate expenseDate;
    private String note;
    private String location;
    private PaymentMethod paymentMethod;
    private Boolean isRecurring;
    private RecurringType recurringType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Long categoryId;
    private String categoryName;
    private String categoryIcon;
}
