package com.sun.expense_management.dto.expense;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ExpenseFilterRequest {
    private String name;
    private Long categoryId;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private Integer page = 0;
    private Integer size = 10;
    private String sortBy = "expenseDate";
    private String sortDir = "desc";
}
