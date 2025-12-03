package com.sun.expense_management.controller;

import com.sun.expense_management.dto.PageResponse;
import com.sun.expense_management.dto.expense.ExpenseFilterRequest;
import com.sun.expense_management.dto.expense.ExpenseRequest;
import com.sun.expense_management.dto.expense.ExpenseResponse;
import com.sun.expense_management.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    /**
     * Lấy danh sách chi tiêu với phân trang và filter
     */
    @GetMapping
    public ResponseEntity<PageResponse<ExpenseResponse>> getExpenses(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "expenseDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        ExpenseFilterRequest filter = new ExpenseFilterRequest();
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

        return ResponseEntity.ok(expenseService.getExpenses(filter));
    }

    /**
     * Lấy chi tiết một khoản chi tiêu
     */
    @GetMapping("/{id}")
    public ResponseEntity<ExpenseResponse> getExpenseById(@PathVariable Long id) {
        return ResponseEntity.ok(expenseService.getExpenseById(id));
    }

    /**
     * Tạo mới khoản chi tiêu
     */
    @PostMapping
    public ResponseEntity<ExpenseResponse> createExpense(@Valid @RequestBody ExpenseRequest request) {
        ExpenseResponse response = expenseService.createExpense(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Cập nhật khoản chi tiêu
     */
    @PutMapping("/{id}")
    public ResponseEntity<ExpenseResponse> updateExpense(
            @PathVariable Long id,
            @Valid @RequestBody ExpenseRequest request
    ) {
        return ResponseEntity.ok(expenseService.updateExpense(id, request));
    }

    /**
     * Xóa khoản chi tiêu
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id) {
        expenseService.deleteExpense(id);
        return ResponseEntity.noContent().build();
    }
}
