package com.sun.expense_management.service;

import com.sun.expense_management.dto.PageResponse;
import com.sun.expense_management.dto.income.IncomeFilterRequest;
import com.sun.expense_management.dto.income.IncomeRequest;
import com.sun.expense_management.dto.income.IncomeResponse;
import com.sun.expense_management.entity.Category;
import com.sun.expense_management.entity.Category.CategoryType;
import com.sun.expense_management.entity.Income;
import com.sun.expense_management.entity.User;
import com.sun.expense_management.exception.ResourceNotFoundException;
import com.sun.expense_management.repository.CategoryRepository;
import com.sun.expense_management.repository.IncomeRepository;
import com.sun.expense_management.repository.UserRepository;
import com.sun.expense_management.util.MessageUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    public IncomeService(IncomeRepository incomeRepository,
                         CategoryRepository categoryRepository,
                         UserRepository userRepository,
                         MessageUtil messageUtil) {
        this.incomeRepository = incomeRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.messageUtil = messageUtil;
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

        Page<Income> incomePage = incomeRepository.findByUserWithFilters(
                user,
                filter.getName(),
                filter.getCategoryId(),
                filter.getStartDate(),
                filter.getEndDate(),
                filter.getMinAmount(),
                filter.getMaxAmount(),
                pageable
        );

        Page<IncomeResponse> responsePage = incomePage.map(IncomeResponse::fromEntity);
        return PageResponse.fromPage(responsePage);
    }

    @Transactional(readOnly = true)
    public IncomeResponse getIncomeById(Long id) {
        User user = getCurrentUser();
        Income income = incomeRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageUtil.getMessage("income.not.found", new Object[]{id})));
        return IncomeResponse.fromEntity(income);
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

        Income income = Income.builder()
                .name(request.getName())
                .amount(request.getAmount())
                .incomeDate(request.getIncomeDate())
                .note(request.getNote())
                .source(request.getSource())
                .isRecurring(request.getIsRecurring())
                .recurringType(request.getRecurringType())
                .user(user)
                .category(category)
                .build();

        income = incomeRepository.save(income);
        return IncomeResponse.fromEntity(income);
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

        income.setName(request.getName());
        income.setAmount(request.getAmount());
        income.setIncomeDate(request.getIncomeDate());
        income.setNote(request.getNote());
        income.setSource(request.getSource());
        income.setIsRecurring(request.getIsRecurring());
        income.setRecurringType(request.getRecurringType());
        income.setCategory(category);

        income = incomeRepository.save(income);
        return IncomeResponse.fromEntity(income);
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
