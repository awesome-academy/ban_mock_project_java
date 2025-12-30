package com.sunasterisk.expense_management.service.admin;

import com.sunasterisk.expense_management.dto.PageResponse;
import com.sunasterisk.expense_management.dto.income.AdminIncomeFilterRequest;
import com.sunasterisk.expense_management.dto.income.IncomeRequest;
import com.sunasterisk.expense_management.dto.income.IncomeResponse;
import com.sunasterisk.expense_management.entity.Income;
import com.sunasterisk.expense_management.entity.User;
import com.sunasterisk.expense_management.exception.ResourceNotFoundException;
import com.sunasterisk.expense_management.mapper.IncomeMapper;
import com.sunasterisk.expense_management.repository.IncomeRepository;
import com.sunasterisk.expense_management.repository.specification.IncomeSpecification;
import com.sunasterisk.expense_management.service.IncomeService;
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
public class AdminIncomeService {

    private final IncomeRepository incomeRepository;
    private final IncomeMapper incomeMapper;
    private final MessageUtil messageUtil;
    private final IncomeService incomeService;
    private final CurrentUserHolder currentUserHolder;

    @Transactional(readOnly = true)
    public PageResponse<IncomeResponse> getAllIncomes(AdminIncomeFilterRequest filter) {
        Sort sort = filter.getSortDir().equalsIgnoreCase("asc")
                ? Sort.by(filter.getSortBy()).ascending()
                : Sort.by(filter.getSortBy()).descending();

        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);

        Specification<Income> spec = IncomeSpecification.withAdminFilters(
                filter.getUserId(),
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
        Income income = incomeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageUtil.getMessage("income.not.found", id)));
        return incomeMapper.toResponse(income);
    }

    @Transactional
    public IncomeResponse updateIncome(Long id, IncomeRequest request) {
        User adminUser = currentUserHolder.getCurrentUserOrThrow();
        return incomeService.updateIncomeInternal(id, request, adminUser);
    }

    @Transactional
    public void deleteIncome(Long id) {
        User adminUser = currentUserHolder.getCurrentUserOrThrow();
        incomeService.deleteIncomeInternal(id, adminUser);
    }
}
