package com.sunasterisk.expense_management.service.admin;

import com.sunasterisk.expense_management.dto.PageResponse;
import com.sunasterisk.expense_management.dto.income.AdminIncomeFilterRequest;
import com.sunasterisk.expense_management.dto.income.IncomeRequest;
import com.sunasterisk.expense_management.dto.income.IncomeResponse;
import com.sunasterisk.expense_management.entity.Category;
import com.sunasterisk.expense_management.entity.Category.CategoryType;
import com.sunasterisk.expense_management.entity.Income;
import com.sunasterisk.expense_management.exception.ResourceNotFoundException;
import com.sunasterisk.expense_management.mapper.IncomeMapper;
import com.sunasterisk.expense_management.repository.CategoryRepository;
import com.sunasterisk.expense_management.repository.IncomeRepository;
import com.sunasterisk.expense_management.repository.specification.IncomeSpecification;
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
    private final CategoryRepository categoryRepository;
    private final IncomeMapper incomeMapper;
    private final MessageUtil messageUtil;

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
        Income income = incomeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageUtil.getMessage("income.not.found", id)));

        Category category = categoryRepository.findByIdAndActiveTrue(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageUtil.getMessage("category.not.found", request.getCategoryId())));

        if (category.getType() != CategoryType.INCOME) {
            throw new IllegalArgumentException(
                    messageUtil.getMessage("category.type.must.be.income"));
        }

        income.setName(request.getName());
        income.setAmount(request.getAmount());
        income.setIncomeDate(request.getIncomeDate());
        income.setNote(request.getNote());
        income.setSource(request.getSource());
        income.setIsRecurring(request.getIsRecurring());
        income.setRecurringType(request.getRecurringType());
        income.setCategory(category);

        Income updatedIncome = incomeRepository.save(income);
        return incomeMapper.toResponse(updatedIncome);
    }

    @Transactional
    public void deleteIncome(Long id) {
        Income income = incomeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageUtil.getMessage("income.not.found", id)));
        incomeRepository.delete(income);
    }
}
