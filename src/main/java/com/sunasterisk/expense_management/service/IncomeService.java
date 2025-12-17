package com.sunasterisk.expense_management.service;

import com.sunasterisk.expense_management.dto.PageResponse;
import com.sunasterisk.expense_management.dto.income.IncomeFilterRequest;
import com.sunasterisk.expense_management.dto.income.IncomeRequest;
import com.sunasterisk.expense_management.dto.income.IncomeResponse;
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

    public IncomeService(IncomeRepository incomeRepository,
                         CategoryRepository categoryRepository,
                         UserRepository userRepository,
                         MessageUtil messageUtil,
                         IncomeMapper incomeMapper) {
        this.incomeRepository = incomeRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.messageUtil = messageUtil;
        this.incomeMapper = incomeMapper;
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
                        messageUtil.getMessage("income.not.found", new Object[]{id})));
        return incomeMapper.toResponse(income);
    }

    @Transactional
    public IncomeResponse createIncome(IncomeRequest request) {
        User user = getCurrentUser();

        Category category = categoryRepository.findByIdAndActiveTrue(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageUtil.getMessage("category.not.found", new Object[]{request.getCategoryId()})));

        if (category.getType() != CategoryType.INCOME) {
            throw new IllegalArgumentException(messageUtil.getMessage("category.invalid.type.income"));
        }

        Income income = incomeMapper.toEntity(request);
        income.setUser(user);
        income.setCategory(category);

        income = incomeRepository.save(income);
        return incomeMapper.toResponse(income);
    }

    @Transactional
    public IncomeResponse updateIncome(Long id, IncomeRequest request) {
        User user = getCurrentUser();

        Income income = incomeRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageUtil.getMessage("income.not.found", new Object[]{id})));

        Category category = categoryRepository.findByIdAndActiveTrue(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageUtil.getMessage("category.not.found", new Object[]{request.getCategoryId()})));

        if (category.getType() != CategoryType.INCOME) {
            throw new IllegalArgumentException(messageUtil.getMessage("category.invalid.type.income"));
        }

        incomeMapper.updateEntity(request, income);
        income.setCategory(category);

        income = incomeRepository.save(income);
        return incomeMapper.toResponse(income);
    }

    @Transactional
    public void deleteIncome(Long id) {
        User user = getCurrentUser();

        Income income = incomeRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageUtil.getMessage("income.not.found", new Object[]{id})));

        incomeRepository.delete(income);
    }
}
