package com.sunasterisk.expense_management.dto.income;

import com.sunasterisk.expense_management.entity.Income;
import com.sunasterisk.expense_management.validation.ValidDateRange;
import com.sunasterisk.expense_management.validation.ValidSortDirection;
import com.sunasterisk.expense_management.validation.ValidSortFields;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@ValidDateRange(message = "{report.date.range.invalid}")
public class IncomeFilterRequest {

    private String name;
    private Long categoryId;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;

    private int page = 0;
    private int size = 20;

    @ValidSortFields(
        entityClass = Income.class,
        allowedFields = {"id", "name", "amount", "incomeDate", "note", "createdAt", "updatedAt"},
        message = "{validation.income.sort.fields.invalid}"
    )
    private String sortBy = "incomeDate";

    @ValidSortDirection
    private String sortDir = "desc";
}