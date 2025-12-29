package com.sunasterisk.expense_management.service.admin;

import com.sunasterisk.expense_management.dto.PageResponse;
import com.sunasterisk.expense_management.dto.expense.AdminExpenseFilterRequest;
import com.sunasterisk.expense_management.dto.expense.ExpenseRequest;
import com.sunasterisk.expense_management.dto.expense.ExpenseResponse;
import com.sunasterisk.expense_management.entity.Expense;
import com.sunasterisk.expense_management.entity.User;
import com.sunasterisk.expense_management.exception.ResourceNotFoundException;
import com.sunasterisk.expense_management.mapper.ExpenseMapper;
import com.sunasterisk.expense_management.repository.ExpenseRepository;
import com.sunasterisk.expense_management.repository.UserRepository;
import com.sunasterisk.expense_management.repository.specification.ExpenseSpecification;
import com.sunasterisk.expense_management.service.ExpenseService;
import com.sunasterisk.expense_management.util.MessageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseMapper expenseMapper;
    private final MessageUtil messageUtil;
    private final ExpenseService expenseService;
    private final UserRepository userRepository;

    private User getCurrentAdminUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(messageUtil.getMessage("user.not.found")));
    }

    @Transactional(readOnly = true)
    public PageResponse<ExpenseResponse> getAllExpenses(AdminExpenseFilterRequest filter) {
        Sort sort = filter.getSortDir().equalsIgnoreCase("asc")
                ? Sort.by(filter.getSortBy()).ascending()
                : Sort.by(filter.getSortBy()).descending();

        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);

        Specification<Expense> spec = ExpenseSpecification.withAdminFilters(
                filter.getUserId(),
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
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageUtil.getMessage("expense.not.found", id)));
        return expenseMapper.toResponse(expense);
    }

    @Transactional
    public ExpenseResponse updateExpense(Long id, ExpenseRequest request) {
        User adminUser = getCurrentAdminUser();
        return expenseService.updateExpenseInternal(id, request, adminUser);
    }

    @Transactional
    public void deleteExpense(Long id) {
        User adminUser = getCurrentAdminUser();
        expenseService.deleteExpenseInternal(id, adminUser);
    }
}
