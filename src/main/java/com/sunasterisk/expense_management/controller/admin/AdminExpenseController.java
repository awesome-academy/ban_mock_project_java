package com.sunasterisk.expense_management.controller.admin;

import com.sunasterisk.expense_management.dto.PageResponse;
import com.sunasterisk.expense_management.dto.expense.AdminExpenseFilterRequest;
import com.sunasterisk.expense_management.dto.expense.ExpenseRequest;
import com.sunasterisk.expense_management.dto.expense.ExpenseResponse;
import com.sunasterisk.expense_management.service.admin.AdminExpenseService;
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
public class AdminExpenseController {

    private final AdminExpenseService adminExpenseService;
    private final CategoryService categoryService;
    private final MessageSource messageSource;

    @GetMapping("/expenses")
    public String index(Model model,
                        @RequestParam(required = false) String name,
                        @RequestParam(required = false) Long userId,
                        @RequestParam(required = false) Long categoryId,
                        @RequestParam(required = false) String startDate,
                        @RequestParam(required = false) String endDate,
                        @RequestParam(defaultValue = "0") Integer page,
                        @RequestParam(defaultValue = "20") Integer size) {
        AdminExpenseFilterRequest.AdminExpenseFilterRequestBuilder filterBuilder = AdminExpenseFilterRequest.builder()
                .name(name)
                .userId(userId)
                .categoryId(categoryId)
                .page(page)
                .size(size);

        if (startDate != null && !startDate.isEmpty()) {
            filterBuilder.startDate(java.time.LocalDate.parse(startDate));
        }
        if (endDate != null && !endDate.isEmpty()) {
            filterBuilder.endDate(java.time.LocalDate.parse(endDate));
        }

        AdminExpenseFilterRequest filter = filterBuilder.build();
        PageResponse<ExpenseResponse> response = adminExpenseService.getAllExpenses(filter);

        model.addAttribute("activeMenu", "expenses");
        model.addAttribute("expenses", response.getContent());
        model.addAttribute("currentPage", response.getPageNumber());
        model.addAttribute("totalPages", response.getTotalPages());
        model.addAttribute("totalElements", response.getTotalElements());
        model.addAttribute("filter", filter);

        return "admin/expenses/index";
    }

    @GetMapping("/expenses/{id}")
    public String detail(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("activeMenu", "expenses");
            ExpenseResponse expense = adminExpenseService.getExpenseById(id);
            model.addAttribute("expense", expense);
            return "admin/expenses/detail";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/expenses";
        }
    }

    @GetMapping("/expenses/{id}/edit")
    public String edit(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("activeMenu", "expenses");
            ExpenseResponse expense = adminExpenseService.getExpenseById(id);

            if (!model.containsAttribute("expenseRequest")) {
                ExpenseRequest expenseRequest = new ExpenseRequest();
                expenseRequest.setId(expense.getId());
                expenseRequest.setName(expense.getName());
                expenseRequest.setAmount(expense.getAmount());
                expenseRequest.setExpenseDate(expense.getExpenseDate());
                expenseRequest.setCategoryId(expense.getCategoryId());
                expenseRequest.setNote(expense.getNote());
                expenseRequest.setLocation(expense.getLocation());
                expenseRequest.setPaymentMethod(expense.getPaymentMethod());
                expenseRequest.setIsRecurring(expense.getIsRecurring());
                expenseRequest.setRecurringType(expense.getRecurringType());
                model.addAttribute("expenseRequest", expenseRequest);
            }

            model.addAttribute("categories", categoryService.getActiveExpenseCategories());
            return "admin/expenses/form";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/expenses";
        }
    }

    @PutMapping("/expenses/{id}")
    public String update(@PathVariable Long id,
                        @Valid @ModelAttribute("expenseRequest") ExpenseRequest expenseRequest,
                        BindingResult bindingResult,
                        Model model,
                        RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("activeMenu", "expenses");
            model.addAttribute("categories", categoryService.getActiveExpenseCategories());
            return "admin/expenses/form";
        }

        try {
            adminExpenseService.updateExpense(id, expenseRequest);
            redirectAttributes.addFlashAttribute("success",
                    messageSource.getMessage("admin.expense.updated.success", null, LocaleContextHolder.getLocale()));
            return "redirect:/admin/expenses";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("expenseRequest", expenseRequest);
            return "redirect:/admin/expenses/" + id + "/edit";
        }
    }

    @DeleteMapping("/expenses/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            adminExpenseService.deleteExpense(id);
            redirectAttributes.addFlashAttribute("success",
                    messageSource.getMessage("admin.expense.deleted.success", null, LocaleContextHolder.getLocale()));
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/expenses";
    }
}
