package com.sunasterisk.expense_management.service;

import com.sunasterisk.expense_management.dto.PageResponse;
import com.sunasterisk.expense_management.dto.expense.ExpenseFilterRequest;
import com.sunasterisk.expense_management.dto.expense.ExpenseRequest;
import com.sunasterisk.expense_management.dto.expense.ExpenseResponse;
import com.sunasterisk.expense_management.entity.ActivityLog.ActionType;
import com.sunasterisk.expense_management.entity.Category;
import com.sunasterisk.expense_management.entity.Category.CategoryType;
import com.sunasterisk.expense_management.entity.Expense;
import com.sunasterisk.expense_management.entity.User;
import com.sunasterisk.expense_management.exception.ResourceNotFoundException;
import com.sunasterisk.expense_management.mapper.ExpenseMapper;
import com.sunasterisk.expense_management.repository.BudgetRepository;
import com.sunasterisk.expense_management.repository.CategoryRepository;
import com.sunasterisk.expense_management.repository.ExpenseRepository;
import com.sunasterisk.expense_management.repository.UserRepository;
import com.sunasterisk.expense_management.repository.specification.ExpenseSpecification;
import com.sunasterisk.expense_management.util.MessageUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final BudgetRepository budgetRepository;
    private final ExpenseMapper expenseMapper;
    private final MessageUtil messageUtil;
    private final ActivityLogService activityLogService;
    private final ObjectMapper objectMapper;

    public ExpenseService(ExpenseRepository expenseRepository,
                          CategoryRepository categoryRepository,
                          UserRepository userRepository,
                          BudgetRepository budgetRepository,
                          ExpenseMapper expenseMapper,
                          MessageUtil messageUtil,
                          ActivityLogService activityLogService,
                          ObjectMapper objectMapper) {
        this.expenseRepository = expenseRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.budgetRepository = budgetRepository;
        this.expenseMapper = expenseMapper;
        this.messageUtil = messageUtil;
        this.activityLogService = activityLogService;
        this.objectMapper = objectMapper;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(messageUtil.getMessage("user.not.found")));
    }

    @Transactional(readOnly = true)
    public PageResponse<ExpenseResponse> getExpenses(ExpenseFilterRequest filter) {
        User user = getCurrentUser();

        Sort sort = filter.getSortDir().equalsIgnoreCase("asc")
                ? Sort.by(filter.getSortBy()).ascending()
                : Sort.by(filter.getSortBy()).descending();

        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);

        // Use Specification for flexible, type-safe dynamic queries
        Specification<Expense> spec = ExpenseSpecification.withFilters(
                user,
                filter.getName(),
                filter.getCategoryId(),
                filter.getStartDate(),
                filter.getEndDate(),
                filter.getMinAmount(),
                filter.getMaxAmount()
        );

        Page<Expense> expensePage = expenseRepository.findAll(spec, pageable);

        Page<ExpenseResponse> responsePage = expensePage.map(expenseMapper::toResponse);
        return PageResponse.fromPage(responsePage);
    }

    @Transactional(readOnly = true)
    public ExpenseResponse getExpenseById(Long id) {
        User user = getCurrentUser();
        Expense expense = expenseRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageUtil.getMessage("expense.not.found", id)));
        return expenseMapper.toResponse(expense);
    }

    @Transactional
    public ExpenseResponse createExpense(ExpenseRequest request) {
        User user = getCurrentUser();

        Category category = categoryRepository.findByIdAndActiveTrue(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageUtil.getMessage("category.not.found", request.getCategoryId())));

        if (category.getType() != CategoryType.EXPENSE) {
            throw new IllegalArgumentException(messageUtil.getMessage("category.invalid.type.expense"));
        }

        Expense expense = expenseMapper.toEntity(request);
        expense.setUser(user);
        expense.setCategory(category);

        expense = expenseRepository.save(expense);

        // Log activity with new values
        try {
            String newValue = objectMapper.writeValueAsString(expenseMapper.toResponse(expense));
            activityLogService.logWithValues(
                ActionType.CREATE,
                user,
                "Expense",
                expense.getId(),
                String.format("Created expense '%s' with amount %.2f", expense.getName(), expense.getAmount()),
                null,
                newValue
            );
        } catch (Exception e) {
            // Fallback to simple log if JSON serialization fails
            activityLogService.log(
                ActionType.CREATE,
                user,
                "Expense",
                expense.getId(),
                String.format("Created expense '%s' with amount %.2f", expense.getName(), expense.getAmount())
            );
        }

        // Update budget's spentAmount
        updateBudgetSpentAmount(user.getId(), category.getId(), expense.getExpenseDate());

        return expenseMapper.toResponse(expense);
    }

    @Transactional
    public ExpenseResponse updateExpense(Long id, ExpenseRequest request) {
        User user = getCurrentUser();

        Expense expense = expenseRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageUtil.getMessage("expense.not.found", id)));

        Category category = categoryRepository.findByIdAndActiveTrue(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageUtil.getMessage("category.not.found", request.getCategoryId())));

        if (category.getType() != CategoryType.EXPENSE) {
            throw new IllegalArgumentException(messageUtil.getMessage("category.invalid.type.expense"));
        }

        // Keep track of old values for budget update and logging
        Long oldCategoryId = expense.getCategory().getId();
        java.time.LocalDate oldDate = expense.getExpenseDate();
        String oldValue = null;
        try {
            oldValue = objectMapper.writeValueAsString(expenseMapper.toResponse(expense));
        } catch (Exception e) {
            // Ignore serialization errors
        }

        expenseMapper.updateEntity(request, expense);
        expense.setCategory(category);

        expense = expenseRepository.save(expense);

        // Build description of changes
        StringBuilder changeDesc = new StringBuilder("Updated expense: ");
        changeDesc.append(expense.getName());

        // Log activity with old and new values
        try {
            String newValue = objectMapper.writeValueAsString(expenseMapper.toResponse(expense));
            activityLogService.logWithValues(
                ActionType.UPDATE,
                user,
                "Expense",
                expense.getId(),
                changeDesc.toString(),
                oldValue,
                newValue
            );
        } catch (Exception e) {
            // Fallback to simple log if JSON serialization fails
            activityLogService.log(
                ActionType.UPDATE,
                user,
                "Expense",
                expense.getId(),
                changeDesc.toString()
            );
        }

        // Update budget's spentAmount for both old and new category/date
        updateBudgetSpentAmount(user.getId(), oldCategoryId, oldDate);
        if (!oldCategoryId.equals(category.getId()) ||
            !java.time.YearMonth.from(oldDate).equals(java.time.YearMonth.from(expense.getExpenseDate()))) {
            updateBudgetSpentAmount(user.getId(), category.getId(), expense.getExpenseDate());
        }

        return expenseMapper.toResponse(expense);
    }

    /**
     * Internal method to update expense - can be called by admin service
     */
    @Transactional
    public ExpenseResponse updateExpenseInternal(Long id, ExpenseRequest request, User actingUser) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageUtil.getMessage("expense.not.found", id)));

        Category category = categoryRepository.findByIdAndActiveTrue(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageUtil.getMessage("category.not.found", request.getCategoryId())));

        if (category.getType() != CategoryType.EXPENSE) {
            throw new IllegalArgumentException(messageUtil.getMessage("category.invalid.type.expense"));
        }

        // Keep old values for logging
        String oldValue = null;
        try {
            oldValue = objectMapper.writeValueAsString(expenseMapper.toResponse(expense));
        } catch (Exception e) {
            // Ignore
        }

        expenseMapper.updateEntity(request, expense);
        expense.setCategory(category);

        expense = expenseRepository.save(expense);

        // Log activity
        try {
            String newValue = objectMapper.writeValueAsString(expenseMapper.toResponse(expense));
            activityLogService.logWithValues(
                ActionType.UPDATE,
                actingUser,
                "Expense",
                expense.getId(),
                "Updated expense: " + expense.getName(),
                oldValue,
                newValue
            );
        } catch (Exception e) {
            activityLogService.log(
                ActionType.UPDATE,
                actingUser,
                "Expense",
                expense.getId(),
                "Updated expense: " + expense.getName()
            );
        }

        return expenseMapper.toResponse(expense);
    }

    @Transactional
    public void deleteExpense(Long id) {
        User user = getCurrentUser();

        Expense expense = expenseRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageUtil.getMessage("expense.not.found", id)));

        Long categoryId = expense.getCategory().getId();
        java.time.LocalDate expenseDate = expense.getExpenseDate();

        // Log activity before deletion with old values
        try {
            String oldValue = objectMapper.writeValueAsString(expenseMapper.toResponse(expense));
            activityLogService.logWithValues(
                ActionType.DELETE,
                user,
                "Expense",
                expense.getId(),
                String.format("Deleted expense '%s' with amount %.2f", expense.getName(), expense.getAmount()),
                oldValue,
                null
            );
        } catch (Exception e) {
            // Fallback to simple log if JSON serialization fails
            activityLogService.log(
                ActionType.DELETE,
                user,
                "Expense",
                expense.getId(),
                String.format("Deleted expense '%s' with amount %.2f", expense.getName(), expense.getAmount())
            );
        }

        expenseRepository.delete(expense);

        // Update budget's spentAmount after deletion
        updateBudgetSpentAmount(user.getId(), categoryId, expenseDate);
    }

    /**
     * Internal method to delete expense - can be called by admin service
     */
    @Transactional
    public void deleteExpenseInternal(Long id, User actingUser) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageUtil.getMessage("expense.not.found", id)));

        // Log activity before deletion
        try {
            String oldValue = objectMapper.writeValueAsString(expenseMapper.toResponse(expense));
            activityLogService.logWithValues(
                ActionType.DELETE,
                actingUser,
                "Expense",
                expense.getId(),
                "Deleted expense: " + expense.getName(),
                oldValue,
                null
            );
        } catch (Exception e) {
            activityLogService.log(
                ActionType.DELETE,
                actingUser,
                "Expense",
                expense.getId(),
                "Deleted expense: " + expense.getName()
            );
        }

        expenseRepository.delete(expense);
    }

    /**
     * Update budget's spentAmount for given category and time period
     * This is called automatically when expense is created/updated/deleted
     *
     * Transaction & Concurrency Handling:
     * - This method runs within a @Transactional context from calling methods
     * - If any exception occurs (including OptimisticLockException), the entire transaction rolls back
     * - When updating expense affects 2 budgets (old and new), both updates are atomic:
     *   * If new budget update fails, old budget update is also rolled back
     *   * This prevents partial state where only one budget is updated
     *
     * Optimistic Locking:
     * - Budget entity uses @Version for optimistic locking
     * - If concurrent updates occur, one will fail with OptimisticLockException
     * - The failed transaction will rollback completely, ensuring data consistency
     * - Client should retry the operation in case of OptimisticLockException
     *
     * @param userId The user ID
     * @param categoryId The category ID
     * @param date The expense date (used to determine year/month)
     * @throws jakarta.persistence.OptimisticLockException if concurrent update detected
     */
    private void updateBudgetSpentAmount(Long userId, Long categoryId, java.time.LocalDate date) {
        java.time.YearMonth yearMonth = java.time.YearMonth.from(date);

        budgetRepository.findByUser_IdAndCategory_IdAndYearAndMonth(
                userId,
                categoryId,
                yearMonth.getYear(),
                yearMonth.getMonthValue()
        ).ifPresent(budget -> {
            // Calculate total spent for this budget period
            java.math.BigDecimal totalSpent = expenseRepository.sumByUserAndCategoryAndYearMonth(
                    userId,
                    categoryId,
                    yearMonth.getYear(),
                    yearMonth.getMonthValue()
            );

            budget.setSpentAmount(totalSpent != null ? totalSpent : java.math.BigDecimal.ZERO);
            budgetRepository.save(budget);
            // @Version field is automatically incremented on save
            // If another transaction modified this budget, OptimisticLockException is thrown
        });
    }
}
