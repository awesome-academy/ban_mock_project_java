package com.sunasterisk.expense_management.controller.admin;

import com.sunasterisk.expense_management.dto.PageResponse;
import com.sunasterisk.expense_management.dto.category.CategoryFilterRequest;
import com.sunasterisk.expense_management.config.RequestLoggingFilter;
import com.sunasterisk.expense_management.dto.CategoryDto;
import com.sunasterisk.expense_management.dto.category.CategoryRequest;
import com.sunasterisk.expense_management.dto.category.CategoryResponse;
import com.sunasterisk.expense_management.service.CategoryService;
import com.sunasterisk.expense_management.entity.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryService categoryService;
    private final MessageSource messageSource;

    @GetMapping("/categories")
    public String index(Model model,
                        @RequestParam(defaultValue = "0") Integer page,
                        @RequestParam(defaultValue = "20") Integer size) {
        CategoryFilterRequest filter = CategoryFilterRequest.builder()
                .page(page)
                .size(size)
                .build();
        PageResponse<CategoryResponse> response = categoryService.getCategories(filter);
        model.addAttribute("activeMenu", "categories");
        model.addAttribute("categories", response.getContent());
        return "admin/categories/index";
    }

    @GetMapping("/categories/new")
    public String newCategory(Model model) {
        model.addAttribute("activeMenu", "categories");
        if (!model.containsAttribute("category")) {
            model.addAttribute("category", new CategoryDto());
        }
        return "admin/categories/form";
    }

    @GetMapping("/categories/{id}")
    public String detail(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("activeMenu", "categories");
            CategoryResponse category = categoryService.getCategoryById(id);
            model.addAttribute("category", category);
            return "admin/categories/detail";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/categories";
        }
    }

    @GetMapping("/categories/{id}/edit")
    public String edit(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("activeMenu", "categories");
            if (!model.containsAttribute("category")) {
                CategoryResponse category = categoryService.getCategoryById(id);
                Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
                log.debug("Editing Category: " + category.getName());
                CategoryDto dto = CategoryDto.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .description(category.getDescription())
                        .icon(category.getIcon())
                        .color(category.getColor())
                        .type(category.getType())
                        .active(category.getActive())
                        .isDefault(category.getIsDefault())
                        .build();
                model.addAttribute("category", dto);
            }
            return "admin/categories/form";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/categories";
        }
    }

    @PostMapping("/categories")
    public String create(@Valid @ModelAttribute("category") CategoryDto category, BindingResult bindingResult,
                         Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("activeMenu", "categories");
            return "admin/categories/form";
        }
        try {
            CategoryRequest request = CategoryRequest.builder()
                    .name(category.getName())
                    .description(category.getDescription())
                    .icon(category.getIcon())
                    .color(category.getColor())
                    .type(category.getType())
                    .active(category.getActive())
                    .build();
            categoryService.createCategory(request);
            redirectAttributes.addFlashAttribute("success",
                    messageSource.getMessage("admin.category.created.success", null, LocaleContextHolder.getLocale()));
            return "redirect:/admin/categories";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("category", category);
            return "redirect:/admin/categories/new";
        }
    }

    @PutMapping("/categories/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("category") CategoryDto category,
                         BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("activeMenu", "categories");
            return "admin/categories/form";
        }
        try {
            CategoryRequest request = CategoryRequest.builder()
                    .name(category.getName())
                    .description(category.getDescription())
                    .icon(category.getIcon())
                    .color(category.getColor())
                    .type(category.getType())
                    .active(category.getActive())
                    .build();
            categoryService.updateCategory(id, request);
            redirectAttributes.addFlashAttribute("success",
                    messageSource.getMessage("admin.category.updated.success", null, LocaleContextHolder.getLocale()));
            return "redirect:/admin/categories";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("category", category);
            return "redirect:/admin/categories/" + id + "/edit";
        }
    }

    @DeleteMapping("/categories/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("success",
                    messageSource.getMessage("admin.category.deleted.success", null, LocaleContextHolder.getLocale()));
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/categories";
    }
}
