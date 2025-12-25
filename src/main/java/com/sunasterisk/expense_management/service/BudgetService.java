package com.sunasterisk.expense_management.service;

import com.sunasterisk.expense_management.dto.PageResponse;
import com.sunasterisk.expense_management.dto.budget.BudgetFilterRequest;
import com.sunasterisk.expense_management.dto.budget.BudgetRequest;
import com.sunasterisk.expense_management.dto.budget.BudgetResponse;
import com.sunasterisk.expense_management.entity.ActivityLog.ActionType;
import com.sunasterisk.expense_management.entity.Budget;
import com.sunasterisk.expense_management.entity.Category;
import com.sunasterisk.expense_management.entity.User;
import com.sunasterisk.expense_management.exception.ResourceNotFoundException;
import com.sunasterisk.expense_management.mapper.BudgetMapper;
import com.sunasterisk.expense_management.repository.BudgetRepository;
import com.sunasterisk.expense_management.repository.CategoryRepository;
import com.sunasterisk.expense_management.repository.UserRepository;
import com.sunasterisk.expense_management.repository.specification.BudgetSpecification;
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
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final BudgetMapper budgetMapper;
    private final MessageUtil messageUtil;
    private final ActivityLogService activityLogService;
    private final ObjectMapper objectMapper;

    public BudgetService(BudgetRepository budgetRepository,
                        CategoryRepository categoryRepository,
                        UserRepository userRepository,
                        BudgetMapper budgetMapper,
                        MessageUtil messageUtil,
                        ActivityLogService activityLogService,
                        ObjectMapper objectMapper) {
        this.budgetRepository = budgetRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.budgetMapper = budgetMapper;
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

    /**
     * Validate category access for the current user
     *
     * @param category The category to validate
     * @param user The current user
     * @throws IllegalArgumentException if user doesn't have access
     * @throws IllegalStateException if data integrity is violated
     */
    private void validateCategoryAccess(Category category, User user) {
        // Default categories are accessible to everyone
        if (category.getIsDefault()) {
            return;
        }

        // Non-default categories must have an owner
        if (category.getUser() == null) {
            throw new IllegalStateException(
                    messageUtil.getMessage("category.integrity.custom.must.have.user"));
        }

        // Check if current user owns this category
        if (!category.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException(
                    messageUtil.getMessage("category.access.denied"));
        }
    }

    @Transactional(readOnly = true)
    public PageResponse<BudgetResponse> getBudgets(BudgetFilterRequest filter) {
        User user = getCurrentUser();

        String[] sortFields = filter.getSortBy().split(",");
        Sort sort = filter.getSortDir().equalsIgnoreCase("asc")
                ? Sort.by(sortFields).ascending()
                : Sort.by(sortFields).descending();

        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);

        // Use Specification for flexible, type-safe dynamic queries
        Specification<Budget> spec = BudgetSpecification.withFilters(
                user,
                filter.getName(),
                filter.getCategoryId(),
                filter.getYear(),
                filter.getMonth(),
                filter.getIsOverBudget(),
                filter.getActive()
        );

        Page<Budget> budgetPage = budgetRepository.findAll(spec, pageable);

        Page<BudgetResponse> responsePage = budgetPage.map(budgetMapper::toResponse);
        return PageResponse.fromPage(responsePage);
    }

    @Transactional(readOnly = true)
    public BudgetResponse getBudgetById(Long id) {
        User user = getCurrentUser();

        Budget budget = budgetRepository.findByIdAndUser_Id(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageUtil.getMessage("budget.not.found", id)));

        return budgetMapper.toResponse(budget);
    }

    @Transactional
    public BudgetResponse createBudget(BudgetRequest request) {
        User user = getCurrentUser();

        // Check if budget already exists for this category, year, month
        boolean exists;
        if (request.getCategoryId() != null) {
            exists = budgetRepository.existsByUser_IdAndCategory_IdAndYearAndMonth(
                    user.getId(),
                    request.getCategoryId(),
                    request.getYear(),
                    request.getMonth()
            );
        } else {
            // Check for duplicate budget without category
            exists = budgetRepository.existsByUser_IdAndCategoryIsNullAndYearAndMonth(
                    user.getId(),
                    request.getYear(),
                    request.getMonth()
            );
        }

        if (exists) {
            throw new IllegalArgumentException(
                    messageUtil.getMessage("budget.already.exists"));
        }

        // Validate category if provided
        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findByIdAndActiveTrue(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            messageUtil.getMessage("category.not.found", request.getCategoryId())));

            validateCategoryAccess(category, user);
        }

        Budget budget = budgetMapper.toEntity(request);
        budget.setUser(user);
        budget.setCategory(category);

        budget = budgetRepository.save(budget);

        // Log activity with new values
        String categoryName = category != null ? category.getName() : "All Categories";
        try {
            String newValue = objectMapper.writeValueAsString(budgetMapper.toResponse(budget));
            activityLogService.logWithValues(
                ActionType.CREATE,
                user,
                "Budget",
                budget.getId(),
                String.format("Created budget for %s (%d/%d) with limit %.2f",
                    categoryName, budget.getMonth(), budget.getYear(), budget.getAmountLimit()),
                null,
                newValue
            );
        } catch (Exception e) {
            activityLogService.log(
                ActionType.CREATE,
                user,
                "Budget",
                budget.getId(),
                String.format("Created budget for %s (%d/%d) with limit %.2f",
                    categoryName, budget.getMonth(), budget.getYear(), budget.getAmountLimit())
            );
        }

        return budgetMapper.toResponse(budget);
    }

    @Transactional
    public BudgetResponse updateBudget(Long id, BudgetRequest request) {
        User user = getCurrentUser();

        Budget budget = budgetRepository.findByIdAndUser_Id(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageUtil.getMessage("budget.not.found", id)));

        // Check if category is being changed
        Long existingCategoryId = budget.getCategory() != null ? budget.getCategory().getId() : null;
        Long newCategoryId = request.getCategoryId();

        boolean categoryChanged = (existingCategoryId == null && newCategoryId != null) ||
                                  (existingCategoryId != null && newCategoryId == null) ||
                                  (existingCategoryId != null && !existingCategoryId.equals(newCategoryId));

        // If category is being changed, check for duplicates
        if (categoryChanged) {
            boolean exists;
            if (newCategoryId != null) {
                exists = budgetRepository.existsByUser_IdAndCategory_IdAndYearAndMonth(
                        user.getId(),
                        newCategoryId,
                        request.getYear(),
                        request.getMonth()
                );
            } else {
                exists = budgetRepository.existsByUser_IdAndCategoryIsNullAndYearAndMonth(
                        user.getId(),
                        request.getYear(),
                        request.getMonth()
                );
            }

            if (exists) {
                throw new IllegalArgumentException(
                        messageUtil.getMessage("budget.already.exists"));
            }
        }

        // Validate and set the new category
        Category category = null;
        if (newCategoryId != null) {
            category = categoryRepository.findByIdAndActiveTrue(newCategoryId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            messageUtil.getMessage("category.not.found", newCategoryId)));

            validateCategoryAccess(category, user);
        }

        // Keep old value for logging
        String oldValue = null;
        try {
            oldValue = objectMapper.writeValueAsString(budgetMapper.toResponse(budget));
        } catch (Exception e) {
            // Ignore
        }

        budgetMapper.updateEntity(request, budget);
        budget.setCategory(category);

        budget = budgetRepository.save(budget);

        // Build description of changes
        String categoryName = category != null ? category.getName() : "All Categories";
        StringBuilder changeDesc = new StringBuilder("Updated budget: ");
        changeDesc.append(categoryName).append(" (").append(budget.getMonth())
                  .append("/").append(budget.getYear()).append(")");

        // Log activity with old and new values
        try {
            String newValue = objectMapper.writeValueAsString(budgetMapper.toResponse(budget));
            activityLogService.logWithValues(
                ActionType.UPDATE,
                user,
                "Budget",
                budget.getId(),
                changeDesc.toString(),
                oldValue,
                newValue
            );
        } catch (Exception e) {
            activityLogService.log(
                ActionType.UPDATE,
                user,
                "Budget",
                budget.getId(),
                changeDesc.toString()
            );
        }

        return budgetMapper.toResponse(budget);
    }

    @Transactional
    public void deleteBudget(Long id) {
        User user = getCurrentUser();

        Budget budget = budgetRepository.findByIdAndUser_Id(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageUtil.getMessage("budget.not.found", id)));

        // Log activity before soft delete with old values
        String categoryName = budget.getCategory() != null ? budget.getCategory().getName() : "All Categories";
        try {
            String oldValue = objectMapper.writeValueAsString(budgetMapper.toResponse(budget));
            activityLogService.logWithValues(
                ActionType.DELETE,
                user,
                "Budget",
                budget.getId(),
                String.format("Deleted budget for %s (%d/%d)",
                    categoryName, budget.getMonth(), budget.getYear()),
                oldValue,
                null
            );
        } catch (Exception e) {
            activityLogService.log(
                ActionType.DELETE,
                user,
                "Budget",
                budget.getId(),
                String.format("Deleted budget for %s (%d/%d)",
                    categoryName, budget.getMonth(), budget.getYear())
            );
        }

        // Soft delete
        budget.setActive(false);
        budgetRepository.save(budget);
    }
}
