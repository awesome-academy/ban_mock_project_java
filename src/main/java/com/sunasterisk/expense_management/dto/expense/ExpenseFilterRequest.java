package com.sunasterisk.expense_management.dto.expense;

import com.sunasterisk.expense_management.entity.Expense;
import com.sunasterisk.expense_management.validation.ValidDateRange;
import com.sunasterisk.expense_management.validation.ValidSortDirection;
import com.sunasterisk.expense_management.validation.ValidSortFields;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@ValidDateRange(message = "{report.date.range.invalid}")
public class ExpenseFilterRequest {
    private String name;
    private Long categoryId;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private Integer page = 0;
    private Integer size = 10;

    @ValidSortFields(
        entityClass = Expense.class,
        allowedFields = {"id", "name", "amount", "expenseDate", "note", "createdAt", "updatedAt"},
        message = "{validation.expense.sort.fields.invalid}"
    )
    private String sortBy = "expenseDate";

    @ValidSortDirection
    private String sortDir = "desc";
}