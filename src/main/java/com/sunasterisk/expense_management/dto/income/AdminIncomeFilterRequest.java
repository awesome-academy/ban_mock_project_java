package com.sunasterisk.expense_management.dto.income;

import com.sunasterisk.expense_management.entity.Income;
import com.sunasterisk.expense_management.validation.ValidDateRange;
import com.sunasterisk.expense_management.validation.ValidSortDirection;
import com.sunasterisk.expense_management.validation.ValidSortFields;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ValidDateRange(message = "{report.date.range.invalid}")
public class AdminIncomeFilterRequest {
    private String name;
    private Long userId;
    private Long categoryId;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;

    @Builder.Default
    private Integer page = 0;

    @Builder.Default
    private Integer size = 20;

    @ValidSortFields(
        entityClass = Income.class,
        allowedFields = {"id", "name", "amount", "incomeDate", "note", "createdAt", "updatedAt"},
        message = "{validation.income.sort.fields.invalid}"
    )
    @Builder.Default
    private String sortBy = "incomeDate";

    @ValidSortDirection
    @Builder.Default
    private String sortDir = "desc";
}
