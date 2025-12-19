package com.sunasterisk.expense_management.service;

import com.sunasterisk.expense_management.config.RequestLoggingFilter;
import com.sunasterisk.expense_management.dto.PageResponse;
import com.sunasterisk.expense_management.dto.category.CategoryFilterRequest;
import com.sunasterisk.expense_management.dto.category.CategoryRequest;
import com.sunasterisk.expense_management.dto.category.CategoryResponse;
import com.sunasterisk.expense_management.entity.Category;
import com.sunasterisk.expense_management.entity.User;
import com.sunasterisk.expense_management.exception.ResourceNotFoundException;
import com.sunasterisk.expense_management.mapper.CategoryMapper;
import com.sunasterisk.expense_management.repository.CategoryRepository;
import com.sunasterisk.expense_management.repository.UserRepository;
import com.sunasterisk.expense_management.repository.specification.CategorySpecification;
import com.sunasterisk.expense_management.util.MessageUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final CategoryMapper categoryMapper;
    private final MessageUtil messageUtil;

    public CategoryService(CategoryRepository categoryRepository,
                          UserRepository userRepository,
                          CategoryMapper categoryMapper,
                          MessageUtil messageUtil) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.categoryMapper = categoryMapper;
        this.messageUtil = messageUtil;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(messageUtil.getMessage("user.not.found")));
    }

    @Transactional(readOnly = true)
    public PageResponse<CategoryResponse> getCategories(CategoryFilterRequest filter) {
        User user = getCurrentUser();

        Sort sort = filter.getSortDir().equalsIgnoreCase("asc")
                ? Sort.by(filter.getSortBy()).ascending()
                : Sort.by(filter.getSortBy()).descending();

        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);

        // Use Specification for flexible, type-safe dynamic queries
        Specification<Category> spec = CategorySpecification.withFilters(
                user,
                filter.getName(),
                filter.getType(),
                filter.getActive()
        );

        Page<Category> categoryPage = categoryRepository.findAll(spec, pageable);

        Page<CategoryResponse> responsePage = categoryPage.map(categoryMapper::toResponse);
        return PageResponse.fromPage(responsePage);
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        User user = getCurrentUser();

        // User can access their own categories + default categories
        Category category = categoryRepository.findById(id)
                .filter(c -> c.getIsDefault() || (c.getUser() != null && c.getUser().getId().equals(user.getId())))
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageUtil.getMessage("category.not.found", id)));

        return categoryMapper.toResponse(category);
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        User user = getCurrentUser();

        Category category = categoryMapper.toEntity(request);
        // If user is not admin, set the user and isDefault fields accordingly
        if (user.getRole().name().equals(User.Role.ADMIN.name())) {
            category.setUser(null); // Admin-created categories are global
            category.setIsDefault(true);
        } else {
            category.setUser(user);
            category.setIsDefault(false);
        }

        category = categoryRepository.save(category);
        return categoryMapper.toResponse(category);
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        User user = getCurrentUser();
        Category category;
        // Admins can update any category
        if (user.getRole().name().equals(User.Role.ADMIN.name())) {
            category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                    messageUtil.getMessage("category.not.found", id)));
        } else {
            // Only allow updating user's own categories (not default categories)
            category = categoryRepository.findByIdAndUser_Id(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    messageUtil.getMessage("category.not.found", id)));
        }

        categoryMapper.updateEntity(request, category);

        category = categoryRepository.save(category);
        return categoryMapper.toResponse(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        User user = getCurrentUser();
        Category category;
        if (user.getRole().name().equals(User.Role.ADMIN.name())) {
            category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                    messageUtil.getMessage("category.not.found", id)));
        } else {
            category = categoryRepository.findByIdAndUser_Id(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    messageUtil.getMessage("category.not.found", id)));
        }

        // Soft delete by setting active = false
        category.setActive(false);
        categoryRepository.save(category);
    }
}
