package com.sunasterisk.expense_management.service;

import com.sunasterisk.expense_management.dto.PageResponse;
import com.sunasterisk.expense_management.dto.income.IncomeFilterRequest;
import com.sunasterisk.expense_management.dto.income.IncomeRequest;
import com.sunasterisk.expense_management.dto.income.IncomeResponse;
import com.sunasterisk.expense_management.entity.ActivityLog.ActionType;
import com.sunasterisk.expense_management.entity.Category;
import com.sunasterisk.expense_management.entity.Category.CategoryType;
import com.sunasterisk.expense_management.entity.Income;
import com.sunasterisk.expense_management.entity.User;
import com.sunasterisk.expense_management.exception.ResourceNotFoundException;
import com.sunasterisk.expense_management.mapper.IncomeMapper;
import com.sunasterisk.expense_management.repository.CategoryRepository;
import com.sunasterisk.expense_management.repository.IncomeRepository;
import com.sunasterisk.expense_management.repository.UserRepository;
import com.sunasterisk.expense_management.repository.specification.IncomeSpecification;
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
public class IncomeService {

    private final IncomeRepository incomeRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final MessageUtil messageUtil;
    private final IncomeMapper incomeMapper;
    private final ActivityLogService activityLogService;
    private final ObjectMapper objectMapper;

    public IncomeService(IncomeRepository incomeRepository,
                         CategoryRepository categoryRepository,
                         UserRepository userRepository,
                         MessageUtil messageUtil,
                         IncomeMapper incomeMapper,
                         ActivityLogService activityLogService,
                         ObjectMapper objectMapper) {
        this.incomeRepository = incomeRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.messageUtil = messageUtil;
        this.incomeMapper = incomeMapper;
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
    public PageResponse<IncomeResponse> getIncomes(IncomeFilterRequest filter) {
        User user = getCurrentUser();

        Sort sort = filter.getSortDir().equalsIgnoreCase("asc")
                ? Sort.by(filter.getSortBy()).ascending()
                : Sort.by(filter.getSortBy()).descending();

        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);

        // Use Specification for flexible, type-safe dynamic queries
        Specification<Income> spec = IncomeSpecification.withFilters(
                user,
                filter.getName(),
                filter.getCategoryId(),
                filter.getStartDate(),
                filter.getEndDate(),
                filter.getMinAmount(),
                filter.getMaxAmount()
        );

        Page<Income> incomePage = incomeRepository.findAll(spec, pageable);

        Page<IncomeResponse> responsePage = incomePage.map(incomeMapper::toResponse);
        return PageResponse.fromPage(responsePage);
    }

    @Transactional(readOnly = true)
    public IncomeResponse getIncomeById(Long id) {
        User user = getCurrentUser();
        Income income = incomeRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageUtil.getMessage("income.not.found", id)));
        return incomeMapper.toResponse(income);
    }

    @Transactional
    public IncomeResponse createIncome(IncomeRequest request) {
        User user = getCurrentUser();

        Category category = categoryRepository.findByIdAndActiveTrue(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageUtil.getMessage("category.not.found", request.getCategoryId())));

        if (category.getType() != CategoryType.INCOME) {
            throw new IllegalArgumentException(messageUtil.getMessage("category.invalid.type.income"));
        }

        Income income = incomeMapper.toEntity(request);
        income.setUser(user);
        income.setCategory(category);

        income = incomeRepository.save(income);

        // Log activity with new values
        try {
            String newValue = objectMapper.writeValueAsString(incomeMapper.toResponse(income));
            activityLogService.logWithValues(
                ActionType.CREATE,
                user,
                "Income",
                income.getId(),
                String.format("Created income '%s' with amount %.2f", income.getName(), income.getAmount()),
                null,
                newValue
            );
        } catch (Exception e) {
            activityLogService.log(
                ActionType.CREATE,
                user,
                "Income",
                income.getId(),
                String.format("Created income '%s' with amount %.2f", income.getName(), income.getAmount())
            );
        }

        return incomeMapper.toResponse(income);
    }

    @Transactional
    public IncomeResponse updateIncome(Long id, IncomeRequest request) {
        User user = getCurrentUser();

        Income income = incomeRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageUtil.getMessage("income.not.found", id)));

        Category category = categoryRepository.findByIdAndActiveTrue(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageUtil.getMessage("category.not.found", request.getCategoryId())));
        if (category.getType() != CategoryType.INCOME) {
            throw new IllegalArgumentException(messageUtil.getMessage("category.invalid.type.income"));
        }

        // Keep old value for logging
        String oldValue = null;
        try {
            oldValue = objectMapper.writeValueAsString(incomeMapper.toResponse(income));
        } catch (Exception e) {
            // Ignore
        }

        incomeMapper.updateEntity(request, income);
        income.setCategory(category);

        income = incomeRepository.save(income);

        // Build description of changes
        StringBuilder changeDesc = new StringBuilder("Updated income: ");
        changeDesc.append(income.getName());

        // Log activity with old and new values
        try {
            String newValue = objectMapper.writeValueAsString(incomeMapper.toResponse(income));
            activityLogService.logWithValues(
                ActionType.UPDATE,
                user,
                "Income",
                income.getId(),
                changeDesc.toString(),
                oldValue,
                newValue
            );
        } catch (Exception e) {
            activityLogService.log(
                ActionType.UPDATE,
                user,
                "Income",
                income.getId(),
                changeDesc.toString()
            );
        }

        return incomeMapper.toResponse(income);
    }

    /**
     * Internal method to update income - can be called by admin service
     */
    @Transactional
    public IncomeResponse updateIncomeInternal(Long id, IncomeRequest request, User actingUser) {
        Income income = incomeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageUtil.getMessage("income.not.found", id)));

        Category category = categoryRepository.findByIdAndActiveTrue(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageUtil.getMessage("category.not.found", request.getCategoryId())));

        if (category.getType() != CategoryType.INCOME) {
            throw new IllegalArgumentException(messageUtil.getMessage("category.invalid.type.income"));
        }

        // Keep old value for logging
        String oldValue = null;
        try {
            oldValue = objectMapper.writeValueAsString(incomeMapper.toResponse(income));
        } catch (Exception e) {
            // Ignore
        }

        incomeMapper.updateEntity(request, income);
        income.setCategory(category);

        income = incomeRepository.save(income);

        // Log activity
        try {
            String newValue = objectMapper.writeValueAsString(incomeMapper.toResponse(income));
            activityLogService.logWithValues(
                ActionType.UPDATE,
                actingUser,
                "Income",
                income.getId(),
                "Updated income: " + income.getName(),
                oldValue,
                newValue
            );
        } catch (Exception e) {
            activityLogService.log(
                ActionType.UPDATE,
                actingUser,
                "Income",
                income.getId(),
                "Updated income: " + income.getName()
            );
        }

        return incomeMapper.toResponse(income);
    }

    @Transactional
    public void deleteIncome(Long id) {
        User user = getCurrentUser();

        Income income = incomeRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageUtil.getMessage("income.not.found", id)));

        // Log activity before deletion with old values
        try {
            String oldValue = objectMapper.writeValueAsString(incomeMapper.toResponse(income));
            activityLogService.logWithValues(
                ActionType.DELETE,
                user,
                "Income",
                income.getId(),
                String.format("Deleted income '%s' with amount %.2f", income.getName(), income.getAmount()),
                oldValue,
                null
            );
        } catch (Exception e) {
            activityLogService.log(
                ActionType.DELETE,
                user,
                "Income",
                income.getId(),
                String.format("Deleted income '%s' with amount %.2f", income.getName(), income.getAmount())
            );
        }

        incomeRepository.delete(income);
    }

    /**
     * Internal method to delete income - can be called by admin service
     */
    @Transactional
    public void deleteIncomeInternal(Long id, User actingUser) {
        Income income = incomeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageUtil.getMessage("income.not.found", id)));

        // Log activity before deletion
        try {
            String oldValue = objectMapper.writeValueAsString(incomeMapper.toResponse(income));
            activityLogService.logWithValues(
                ActionType.DELETE,
                actingUser,
                "Income",
                income.getId(),
                "Deleted income: " + income.getName(),
                oldValue,
                null
            );
        } catch (Exception e) {
            activityLogService.log(
                ActionType.DELETE,
                actingUser,
                "Income",
                income.getId(),
                "Deleted income: " + income.getName()
            );
        }

        incomeRepository.delete(income);
    }
}
