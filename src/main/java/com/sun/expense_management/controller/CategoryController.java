package com.sun.expense_management.controller;

import com.sun.expense_management.dto.PageResponse;
import com.sun.expense_management.dto.category.CategoryFilterRequest;
import com.sun.expense_management.dto.category.CategoryRequest;
import com.sun.expense_management.dto.category.CategoryResponse;
import com.sun.expense_management.entity.Category.CategoryType;
import com.sun.expense_management.service.CategoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * GET /api/categories - Get paginated list of categories with filters
     */
    @GetMapping
    public ResponseEntity<PageResponse<CategoryResponse>> getCategories(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) CategoryType type,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer page,
            @RequestParam(defaultValue = "10") @Positive @Max(100) Integer size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        CategoryFilterRequest filter = CategoryFilterRequest.builder()
                .name(name)
                .type(type)
                .active(active)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDir(sortDir)
                .build();

        PageResponse<CategoryResponse> response = categoryService.getCategories(filter);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/categories/{id} - Get category by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long id) {
        CategoryResponse category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }

    /**
     * POST /api/categories - Create new category
     */
    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request) {
        CategoryResponse category = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(category);
    }

    /**
     * PUT /api/categories/{id} - Update category
     */
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request
    ) {
        CategoryResponse category = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(category);
    }

    /**
     * DELETE /api/categories/{id} - Soft delete category
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
