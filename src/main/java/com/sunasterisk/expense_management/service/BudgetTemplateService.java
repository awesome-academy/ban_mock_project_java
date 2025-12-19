package com.sunasterisk.expense_management.service;

import com.sunasterisk.expense_management.dto.PageResponse;
import com.sunasterisk.expense_management.dto.budgettemplate.BudgetTemplateFilterRequest;
import com.sunasterisk.expense_management.dto.budgettemplate.BudgetTemplateItemDto;
import com.sunasterisk.expense_management.dto.budgettemplate.BudgetTemplateRequest;
import com.sunasterisk.expense_management.dto.budgettemplate.BudgetTemplateResponse;
import com.sunasterisk.expense_management.entity.BudgetTemplate;
import com.sunasterisk.expense_management.entity.BudgetTemplateItem;
import com.sunasterisk.expense_management.entity.Category;
import com.sunasterisk.expense_management.exception.ResourceNotFoundException;
import com.sunasterisk.expense_management.mapper.BudgetTemplateMapper;
import com.sunasterisk.expense_management.repository.BudgetTemplateItemRepository;
import com.sunasterisk.expense_management.repository.BudgetTemplateRepository;
import com.sunasterisk.expense_management.repository.CategoryRepository;
import com.sunasterisk.expense_management.repository.specification.BudgetTemplateSpecification;
import com.sunasterisk.expense_management.util.MessageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetTemplateService {

    private final BudgetTemplateRepository budgetTemplateRepository;
    private final BudgetTemplateItemRepository budgetTemplateItemRepository;
    private final CategoryRepository categoryRepository;
    private final BudgetTemplateMapper budgetTemplateMapper;
    private final MessageUtil messageUtil;

    @Transactional(readOnly = true)
    public PageResponse<BudgetTemplateResponse> getBudgetTemplates(BudgetTemplateFilterRequest filter) {
        Sort sort = filter.getSortDir().equalsIgnoreCase("asc")
                ? Sort.by(filter.getSortBy()).ascending()
                : Sort.by(filter.getSortBy()).descending();

        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);

        Specification<BudgetTemplate> spec = BudgetTemplateSpecification.withFilters(
                filter.getName(),
                filter.getActive()
        );

        Page<BudgetTemplate> templatePage = budgetTemplateRepository.findAll(spec, pageable);
        Page<BudgetTemplateResponse> responsePage = templatePage.map(budgetTemplateMapper::toResponse);

        return PageResponse.fromPage(responsePage);
    }

    @Transactional(readOnly = true)
    public BudgetTemplateResponse getBudgetTemplateById(Long id) {
        BudgetTemplate template = budgetTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageUtil.getMessage("budget.template.not.found", id)));

        return budgetTemplateMapper.toResponse(template);
    }

    @Transactional
    public BudgetTemplateResponse createBudgetTemplate(BudgetTemplateRequest request) {
        BudgetTemplate template = budgetTemplateMapper.toEntity(request);

        // Save template first
        BudgetTemplate savedTemplate = budgetTemplateRepository.save(template);

        // Process and save items
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            List<BudgetTemplateItem> items = new ArrayList<>();
            for (BudgetTemplateItemDto itemDto : request.getItems()) {
                Category category = categoryRepository.findById(itemDto.getCategoryId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                messageUtil.getMessage("category.not.found", itemDto.getCategoryId())));

                BudgetTemplateItem item = BudgetTemplateItem.builder()
                        .budgetTemplate(savedTemplate)
                        .category(category)
                        .defaultAmount(itemDto.getDefaultAmount())
                        .build();
                items.add(item);
            }
            savedTemplate.setItems(items);
            budgetTemplateItemRepository.saveAll(items);
        }

        return budgetTemplateMapper.toResponse(savedTemplate);
    }

    @Transactional
    public BudgetTemplateResponse updateBudgetTemplate(Long id, BudgetTemplateRequest request) {
        BudgetTemplate template = budgetTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageUtil.getMessage("budget.template.not.found", id)));

        // Update basic fields
        budgetTemplateMapper.updateEntity(request, template);

        // Update items using clear() to leverage orphanRemoval
        template.getItems().clear();

        // Add new items
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            for (BudgetTemplateItemDto itemDto : request.getItems()) {
                Category category = categoryRepository.findById(itemDto.getCategoryId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                messageUtil.getMessage("category.not.found", itemDto.getCategoryId())));

                BudgetTemplateItem item = BudgetTemplateItem.builder()
                        .budgetTemplate(template)
                        .category(category)
                        .defaultAmount(itemDto.getDefaultAmount())
                        .build();
                template.getItems().add(item);
            }
        }

        BudgetTemplate savedTemplate = budgetTemplateRepository.save(template);
        return budgetTemplateMapper.toResponse(savedTemplate);
    }

    @Transactional
    public void deleteBudgetTemplate(Long id) {
        BudgetTemplate template = budgetTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageUtil.getMessage("budget.template.not.found", id)));

        budgetTemplateRepository.delete(template);
    }

    @Transactional(readOnly = true)
    public List<BudgetTemplateResponse> getAllActiveBudgetTemplates() {
        return budgetTemplateRepository.findByActiveTrue().stream()
                .map(budgetTemplateMapper::toResponse)
                .toList();
    }
}
