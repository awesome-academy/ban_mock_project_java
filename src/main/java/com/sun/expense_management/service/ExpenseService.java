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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    public ExpenseService(ExpenseRepository expenseRepository,
                          CategoryRepository categoryRepository,
                          UserRepository userRepository,
                          ExpenseMapper expenseMapper) {
        this.expenseRepository = expenseRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.expenseMapper = expenseMapper;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User không tồn tại"));
    }

    @Transactional(readOnly = true)
    public PageResponse<ExpenseResponse> getExpenses(ExpenseFilterRequest filter) {
        User user = getCurrentUser();

        Sort sort = filter.getSortDir().equalsIgnoreCase("asc")
                ? Sort.by(filter.getSortBy()).ascending()
                : Sort.by(filter.getSortBy()).descending();

        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);

        Page<Expense> expensePage = expenseRepository.findByUserWithFilters(
                user,
                filter.getName(),
                filter.getCategoryId(),
                filter.getStartDate(),
                filter.getEndDate(),
                filter.getMinAmount(),
                filter.getMaxAmount(),
                pageable
        );

        Page<ExpenseResponse> responsePage = expensePage.map(expenseMapper::toResponse);
        return PageResponse.fromPage(responsePage);
    }

    @Transactional(readOnly = true)
    public ExpenseResponse getExpenseById(Long id) {
        User user = getCurrentUser();
        Expense expense = expenseRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chi tiêu với id: " + id));
        return expenseMapper.toResponse(expense);
    }

    @Transactional
    public ExpenseResponse createExpense(ExpenseRequest request) {
        User user = getCurrentUser();

        Category category = categoryRepository.findByIdAndActiveTrue(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục với id: " + request.getCategoryId()));

        if (category.getType() != CategoryType.EXPENSE) {
            throw new IllegalArgumentException("Danh mục không phải loại chi tiêu");
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
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chi tiêu với id: " + id));

        Category category = categoryRepository.findByIdAndActiveTrue(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục với id: " + request.getCategoryId()));

        if (category.getType() != CategoryType.EXPENSE) {
            throw new IllegalArgumentException("Danh mục không phải loại chi tiêu");
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
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chi tiêu với id: " + id));

        expenseRepository.delete(expense);
    }
}
