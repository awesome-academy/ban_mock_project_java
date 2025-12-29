package com.sunasterisk.expense_management.service.admin;

import com.sunasterisk.expense_management.dto.PageResponse;
import com.sunasterisk.expense_management.dto.budget.AdminBudgetFilterRequest;
import com.sunasterisk.expense_management.dto.budget.BudgetRequest;
import com.sunasterisk.expense_management.dto.budget.BudgetResponse;
import com.sunasterisk.expense_management.entity.Budget;
import com.sunasterisk.expense_management.entity.User;
import com.sunasterisk.expense_management.exception.ResourceNotFoundException;
import com.sunasterisk.expense_management.mapper.BudgetMapper;
import com.sunasterisk.expense_management.repository.BudgetRepository;
import com.sunasterisk.expense_management.repository.specification.BudgetSpecification;
import com.sunasterisk.expense_management.service.BudgetService;
import com.sunasterisk.expense_management.util.CurrentUserHolder;
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
public class AdminBudgetService {

    private final BudgetRepository budgetRepository;
    private final BudgetMapper budgetMapper;
    private final MessageUtil messageUtil;
    private final BudgetService budgetService;
    private final CurrentUserHolder currentUserHolder;

    @Transactional(readOnly = true)
    public PageResponse<BudgetResponse> getAllBudgets(AdminBudgetFilterRequest filter) {
        String[] sortFields = filter.getSortBy().split(",");
        Sort sort = filter.getSortDir().equalsIgnoreCase("asc")
                ? Sort.by(sortFields).ascending()
                : Sort.by(sortFields).descending();

        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);

        Specification<Budget> spec = BudgetSpecification.withAdminFilters(
                filter.getUserId(),
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
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageUtil.getMessage("budget.not.found", id)));
        return budgetMapper.toResponse(budget);
    }

    @Transactional
    public BudgetResponse updateBudget(Long id, BudgetRequest request) {
        User adminUser = currentUserHolder.getCurrentUserOrThrow();
        return budgetService.updateBudgetInternal(id, request, adminUser);
    }

    @Transactional
    public void deleteBudget(Long id) {
        User adminUser = currentUserHolder.getCurrentUserOrThrow();
        budgetService.deleteBudgetInternal(id, adminUser);
    }
}
