package com.sunasterisk.expense_management.service.admin;

import com.sunasterisk.expense_management.dto.PageResponse;
import com.sunasterisk.expense_management.dto.expense.AdminExpenseFilterRequest;
import com.sunasterisk.expense_management.dto.expense.ExpenseRequest;
import com.sunasterisk.expense_management.dto.expense.ExpenseResponse;
import com.sunasterisk.expense_management.entity.Category;
import com.sunasterisk.expense_management.entity.Category.CategoryType;
import com.sunasterisk.expense_management.entity.Expense;
import com.sunasterisk.expense_management.exception.ResourceNotFoundException;
import com.sunasterisk.expense_management.mapper.ExpenseMapper;
import com.sunasterisk.expense_management.repository.CategoryRepository;
import com.sunasterisk.expense_management.repository.ExpenseRepository;
import com.sunasterisk.expense_management.repository.specification.ExpenseSpecification;
import com.sunasterisk.expense_management.util.MessageUtil;
import lombok.RequiredArgsConstructor;
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
    private final CategoryRepository categoryRepository;
    private final ExpenseMapper expenseMapper;
    private final MessageUtil messageUtil;

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
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageUtil.getMessage("expense.not.found", id)));

        Category category = categoryRepository.findByIdAndActiveTrue(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageUtil.getMessage("category.not.found", request.getCategoryId())));

        if (category.getType() != CategoryType.EXPENSE) {
            throw new IllegalArgumentException(
                    messageUtil.getMessage("category.type.must.be.expense"));
        }

        expense.setName(request.getName());
        expense.setAmount(request.getAmount());
        expense.setExpenseDate(request.getExpenseDate());
        expense.setNote(request.getNote());
        expense.setLocation(request.getLocation());
        expense.setPaymentMethod(request.getPaymentMethod());
        expense.setIsRecurring(request.getIsRecurring());
        expense.setRecurringType(request.getRecurringType());
        expense.setCategory(category);

        Expense updatedExpense = expenseRepository.save(expense);
        return expenseMapper.toResponse(updatedExpense);
    }

    @Transactional
    public void deleteExpense(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageUtil.getMessage("expense.not.found", id)));
        expenseRepository.delete(expense);
    }
}
