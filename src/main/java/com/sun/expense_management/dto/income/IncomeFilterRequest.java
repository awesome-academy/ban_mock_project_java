package com.sun.expense_management.dto.income;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class IncomeFilterRequest {

    private String name;
    private Long categoryId;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;

    private int page = 0;
    private int size = 20;
    private String sortBy = "incomeDate";
    private String sortDir = "desc";
}
