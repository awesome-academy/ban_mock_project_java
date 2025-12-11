package com.sun.expense_management.controller;

import com.sun.expense_management.dto.PageResponse;
import com.sun.expense_management.dto.income.IncomeFilterRequest;
import com.sun.expense_management.dto.income.IncomeRequest;
import com.sun.expense_management.dto.income.IncomeResponse;
import com.sun.expense_management.service.IncomeService;
import com.sun.expense_management.util.MessageUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/api/incomes")
public class IncomeController {

    private final IncomeService incomeService;
    private final MessageUtil messageUtil;

    public IncomeController(IncomeService incomeService, MessageUtil messageUtil) {
        this.incomeService = incomeService;
        this.messageUtil = messageUtil;
    }

    /**
     * Lấy danh sách thu nhập với phân trang và filter
     */
    @GetMapping
    public ResponseEntity<PageResponse<IncomeResponse>> getIncomes(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) @Positive Long categoryId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) @DecimalMin("0.0") BigDecimal minAmount,
            @RequestParam(required = false) @DecimalMin("0.0") BigDecimal maxAmount,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer page,
            @RequestParam(defaultValue = "10") @Positive @Max(100) Integer size,
            @RequestParam(defaultValue = "incomeDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        IncomeFilterRequest filter = new IncomeFilterRequest();
        filter.setName(name);
        filter.setCategoryId(categoryId);
        filter.setStartDate(startDate);
        filter.setEndDate(endDate);
        filter.setMinAmount(minAmount);
        filter.setMaxAmount(maxAmount);
        filter.setPage(page);
        filter.setSize(size);
        filter.setSortBy(sortBy);
        filter.setSortDir(sortDir);

        return ResponseEntity.ok(incomeService.getIncomes(filter));
    }

    /**
     * Lấy chi tiết một khoản thu nhập
     */
    @GetMapping("/{id}")
    public ResponseEntity<IncomeResponse> getIncomeById(@PathVariable Long id) {
        return ResponseEntity.ok(incomeService.getIncomeById(id));
    }

    /**
     * Tạo mới khoản thu nhập
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createIncome(@Valid @RequestBody IncomeRequest request) {
        IncomeResponse response = incomeService.createIncome(request);

        Map<String, Object> result = new HashMap<>();
        result.put("message", messageUtil.getMessage("income.created.success"));
        result.put("data", response);

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * Cập nhật khoản thu nhập
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateIncome(
            @PathVariable Long id,
            @Valid @RequestBody IncomeRequest request
    ) {
        IncomeResponse response = incomeService.updateIncome(id, request);

        Map<String, Object> result = new HashMap<>();
        result.put("message", messageUtil.getMessage("income.updated.success"));
        result.put("data", response);

        return ResponseEntity.ok(result);
    }

    /**
     * Xóa khoản thu nhập
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteIncome(@PathVariable Long id) {
        incomeService.deleteIncome(id);

        Map<String, String> result = new HashMap<>();
        result.put("message", messageUtil.getMessage("income.deleted.success"));

        return ResponseEntity.ok(result);
    }
}
