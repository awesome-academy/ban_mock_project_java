package com.sunasterisk.expense_management.controller.admin;

import com.sunasterisk.expense_management.dto.PageResponse;
import com.sunasterisk.expense_management.dto.budgettemplate.BudgetTemplateFilterRequest;
import com.sunasterisk.expense_management.dto.budgettemplate.BudgetTemplateRequest;
import com.sunasterisk.expense_management.dto.budgettemplate.BudgetTemplateResponse;
import com.sunasterisk.expense_management.service.BudgetTemplateService;
import com.sunasterisk.expense_management.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
public class AdminBudgetTemplateController {

    private final BudgetTemplateService budgetTemplateService;
    private final CategoryService categoryService;
    private final MessageSource messageSource;

    @GetMapping("/budget-templates")
    public String index(Model model,
                        @RequestParam(defaultValue = "0") Integer page,
                        @RequestParam(defaultValue = "20") Integer size) {
        BudgetTemplateFilterRequest filter = BudgetTemplateFilterRequest.builder()
                .page(page)
                .size(size)
                .build();
        PageResponse<BudgetTemplateResponse> response = budgetTemplateService.getBudgetTemplates(filter);
        model.addAttribute("activeMenu", "budget-templates");
        model.addAttribute("templates", response.getContent());
        return "admin/budget-templates/index";
    }

    @GetMapping("/budget-templates/new")
    public String newBudgetTemplate(Model model) {
        model.addAttribute("activeMenu", "budget-templates");
        if (!model.containsAttribute("template")) {
            model.addAttribute("template", new BudgetTemplateRequest());
        }
        // Load active categories for dropdown
        model.addAttribute("categories", categoryService.getActiveCategories());
        return "admin/budget-templates/form";
    }

    @GetMapping("/budget-templates/{id}")
    public String detail(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("activeMenu", "budget-templates");
            BudgetTemplateResponse template = budgetTemplateService.getBudgetTemplateById(id);
            model.addAttribute("template", template);
            return "admin/budget-templates/detail";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/budget-templates";
        }
    }

    @GetMapping("/budget-templates/{id}/edit")
    public String edit(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("activeMenu", "budget-templates");
            if (!model.containsAttribute("template")) {
                BudgetTemplateResponse template = budgetTemplateService.getBudgetTemplateById(id);
                BudgetTemplateRequest request = BudgetTemplateRequest.builder()
                        .name(template.getName())
                        .description(template.getDescription())
                        .active(template.getActive())
                        .items(template.getItems())
                        .build();
                model.addAttribute("template", request);
            }
            // Always set templateId for edit mode
            if (!model.containsAttribute("templateId")) {
                model.addAttribute("templateId", id);
            }
            // Load active categories for dropdown
            model.addAttribute("categories", categoryService.getActiveCategories());
            return "admin/budget-templates/form";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/budget-templates";
        }
    }

    @PostMapping("/budget-templates")
    public String create(@Valid @ModelAttribute("template") BudgetTemplateRequest template,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("activeMenu", "budget-templates");
            model.addAttribute("categories", categoryService.getActiveCategories());
            return "admin/budget-templates/form";
        }
        try {
            budgetTemplateService.createBudgetTemplate(template);
            redirectAttributes.addFlashAttribute("success",
                    messageSource.getMessage("admin.budget.template.created.success", null, LocaleContextHolder.getLocale()));
            return "redirect:/admin/budget-templates";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("template", template);
            return "redirect:/admin/budget-templates/new";
        }
    }

    @PutMapping("/budget-templates/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("template") BudgetTemplateRequest template,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("activeMenu", "budget-templates");
            model.addAttribute("categories", categoryService.getActiveCategories());
            model.addAttribute("templateId", id);
            return "admin/budget-templates/form";
        }
        try {
            budgetTemplateService.updateBudgetTemplate(id, template);
            redirectAttributes.addFlashAttribute("success",
                    messageSource.getMessage("admin.budget.template.updated.success", null, LocaleContextHolder.getLocale()));
            return "redirect:/admin/budget-templates";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("template", template);
            redirectAttributes.addFlashAttribute("templateId", id);
            return "redirect:/admin/budget-templates/" + id + "/edit";
        }
    }

    @DeleteMapping("/budget-templates/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            budgetTemplateService.deleteBudgetTemplate(id);
            redirectAttributes.addFlashAttribute("success",
                    messageSource.getMessage("admin.budget.template.deleted.success", null, LocaleContextHolder.getLocale()));
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/budget-templates";
    }
}
