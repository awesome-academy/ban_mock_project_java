package com.sunasterisk.expense_management.controller.admin;

import com.sunasterisk.expense_management.dto.PageResponse;
import com.sunasterisk.expense_management.dto.budget.AdminBudgetFilterRequest;
import com.sunasterisk.expense_management.dto.budget.BudgetRequest;
import com.sunasterisk.expense_management.dto.budget.BudgetResponse;
import com.sunasterisk.expense_management.service.admin.AdminBudgetService;
import com.sunasterisk.expense_management.service.CategoryService;
import com.sunasterisk.expense_management.service.CsvExportService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminBudgetController extends BaseAdminController {

    private static final String MODULE = "budgets";

    private final AdminBudgetService adminBudgetService;
    private final CategoryService categoryService;
    private final CsvExportService csvExportService;

    public AdminBudgetController(AdminBudgetService adminBudgetService,
                                CategoryService categoryService,
                                CsvExportService csvExportService,
                                MessageSource messageSource) {
        super(messageSource);
        this.adminBudgetService = adminBudgetService;
        this.categoryService = categoryService;
        this.csvExportService = csvExportService;
    }

    @GetMapping("/budgets")
    public String index(Model model,
                        @RequestParam(required = false) Long userId,
                        @RequestParam(required = false) String name,
                        @RequestParam(required = false) Long categoryId,
                        @RequestParam(required = false) Integer year,
                        @RequestParam(required = false) Integer month,
                        @RequestParam(required = false) Boolean isOverBudget,
                        @RequestParam(required = false) Boolean active,
                        @RequestParam(defaultValue = "0") Integer page,
                        @RequestParam(defaultValue = "1") Integer size) {

        AdminBudgetFilterRequest filter = AdminBudgetFilterRequest.builder()
                .userId(userId)
                .name(name)
                .categoryId(categoryId)
                .year(year)
                .month(month)
                .isOverBudget(isOverBudget)
                .active(active)
                .page(page)
                .size(size)
                .build();

        PageResponse<BudgetResponse> response = adminBudgetService.getAllBudgets(filter);

        model.addAttribute("activeMenu", MODULE);
        model.addAttribute("budgets", response.getContent());
        model.addAttribute("currentPage", response.getPageNumber());
        model.addAttribute("totalPages", response.getTotalPages());
        model.addAttribute("totalElements", response.getTotalElements());
        model.addAttribute("filter", filter);

        return viewIndex(MODULE);
    }

    @GetMapping("/budgets/{id}")
    public String detail(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("activeMenu", MODULE);
            BudgetResponse budget = adminBudgetService.getBudgetById(id);
            model.addAttribute("budget", budget);
            return viewDetail(MODULE);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return redirectToIndex(MODULE);
        }
    }

    @GetMapping("/budgets/{id}/edit")
    public String edit(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("activeMenu", MODULE);
            model.addAttribute("budgetId", id);
            if (!model.containsAttribute("budgetRequest")) {
                BudgetResponse budget = adminBudgetService.getBudgetById(id);
                BudgetRequest request = BudgetRequest.builder()
                        .name(budget.getName())
                        .amountLimit(budget.getAmountLimit())
                        .year(budget.getYear())
                        .month(budget.getMonth())
                        .categoryId(budget.getCategoryId())
                        .note(budget.getNote())
                        .alertThreshold(budget.getAlertThreshold())
                        .active(budget.getActive())
                        .build();
                model.addAttribute("budgetRequest", request);
            }
            model.addAttribute("categories", categoryService.getActiveCategories());
            return viewForm(MODULE);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return redirectToIndex(MODULE);
        }
    }

    @PutMapping("/budgets/{id}")
    public String update(@PathVariable Long id,
                        @Valid @ModelAttribute("budgetRequest") BudgetRequest budgetRequest,
                        BindingResult bindingResult,
                        Model model,
                        RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("activeMenu", MODULE);
            model.addAttribute("budgetId", id);
            model.addAttribute("categories", categoryService.getActiveCategories());
            return viewForm(MODULE);
        }

        try {
            adminBudgetService.updateBudget(id, budgetRequest);
            redirectAttributes.addFlashAttribute("success",
                    getMessage("admin.budget.updated.success"));
            return redirectToIndex(MODULE);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("budgetRequest", budgetRequest);
            redirectAttributes.addFlashAttribute("budgetId", id);
            return redirectToEdit(MODULE, id);
        }
    }

    @DeleteMapping("/budgets/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            adminBudgetService.deleteBudget(id);
            redirectAttributes.addFlashAttribute("success",
                    getMessage("admin.budget.deleted.success"));
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return redirectToIndex(MODULE);
    }

    /**
     * Export budgets to CSV
     */
    @GetMapping("/budgets/export")
    public void exportBudgets(HttpServletResponse response) {
        try {
            csvExportService.exportBudgets(response);
        } catch (Exception e) {
            throw new RuntimeException(getMessage("admin.budget.export.failed") + ": " + e.getMessage(), e);
        }
    }
}
