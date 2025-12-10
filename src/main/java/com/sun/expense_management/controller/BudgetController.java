package com.sun.expense_management.controller;

import com.sun.expense_management.dto.PageResponse;
import com.sun.expense_management.dto.budget.BudgetFilterRequest;
import com.sun.expense_management.dto.budget.BudgetRequest;
import com.sun.expense_management.dto.budget.BudgetResponse;
import com.sun.expense_management.service.BudgetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    /**
     * GET /api/budgets - Get paginated list of budgets with filters
     */
    @GetMapping
    public ResponseEntity<PageResponse<BudgetResponse>> getBudgets(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Boolean isOverBudget,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "year,month") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        BudgetFilterRequest filter = BudgetFilterRequest.builder()
                .name(name)
                .categoryId(categoryId)
                .year(year)
                .month(month)
                .isOverBudget(isOverBudget)
                .active(active)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDir(sortDir)
                .build();

        PageResponse<BudgetResponse> response = budgetService.getBudgets(filter);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/budgets/{id} - Get budget by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<BudgetResponse> getBudgetById(@PathVariable Long id) {
        BudgetResponse budget = budgetService.getBudgetById(id);
        return ResponseEntity.ok(budget);
    }

    /**
     * POST /api/budgets - Create new budget
     */
    @PostMapping
    public ResponseEntity<BudgetResponse> createBudget(@Valid @RequestBody BudgetRequest request) {
        BudgetResponse budget = budgetService.createBudget(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(budget);
    }

    /**
     * PUT /api/budgets/{id} - Update budget
     */
    @PutMapping("/{id}")
    public ResponseEntity<BudgetResponse> updateBudget(
            @PathVariable Long id,
            @Valid @RequestBody BudgetRequest request
    ) {
        BudgetResponse budget = budgetService.updateBudget(id, request);
        return ResponseEntity.ok(budget);
    }

    /**
     * DELETE /api/budgets/{id} - Soft delete budget
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(@PathVariable Long id) {
        budgetService.deleteBudget(id);
        return ResponseEntity.noContent().build();
    }
}
