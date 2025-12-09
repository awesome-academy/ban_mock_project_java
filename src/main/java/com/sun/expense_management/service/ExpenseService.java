package com.sun.expense_management.service;

import com.sun.expense_management.dto.PageResponse;
import com.sun.expense_management.dto.expense.ExpenseFilterRequest;
import com.sun.expense_management.dto.expense.ExpenseRequest;
import com.sun.expense_management.dto.expense.ExpenseResponse;
import com.sun.expense_management.entity.Category;
import com.sun.expense_management.entity.Category.CategoryType;
import com.sun.expense_management.entity.Expense;
import com.sun.expense_management.entity.User;
import com.sun.expense_management.exception.ResourceNotFoundException;
import com.sun.expense_management.mapper.ExpenseMapper;
import com.sun.expense_management.repository.CategoryRepository;
import com.sun.expense_management.repository.ExpenseRepository;
import com.sun.expense_management.repository.UserRepository;
import com.sun.expense_management.repository.specification.ExpenseSpecification;
import com.sun.expense_management.util.MessageUtil;
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
    private final ExpenseMapper expenseMapper;
    private final MessageUtil messageUtil;

    public ExpenseService(ExpenseRepository expenseRepository,
                          CategoryRepository categoryRepository,
                          UserRepository userRepository,
                          ExpenseMapper expenseMapper,
                          MessageUtil messageUtil) {
        this.expenseRepository = expenseRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.expenseMapper = expenseMapper;
        this.messageUtil = messageUtil;
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

        expenseMapper.updateEntity(request, expense);
        expense.setCategory(category);

        expense = expenseRepository.save(expense);
        return expenseMapper.toResponse(expense);
    }

    @Transactional
    public void deleteExpense(Long id) {
        User user = getCurrentUser();

        Expense expense = expenseRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageUtil.getMessage("expense.not.found", id)));

        expenseRepository.delete(expense);
    }
}
