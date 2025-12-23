package com.sunasterisk.expense_management.controller.admin;

import com.sunasterisk.expense_management.dto.PageResponse;
import com.sunasterisk.expense_management.dto.income.AdminIncomeFilterRequest;
import com.sunasterisk.expense_management.dto.income.IncomeRequest;
import com.sunasterisk.expense_management.dto.income.IncomeResponse;
import com.sunasterisk.expense_management.service.admin.AdminIncomeService;
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
public class AdminIncomeController {

    private final CategoryService categoryService;
    private final AdminIncomeService adminIncomeService;
    private final MessageSource messageSource;

    @GetMapping("/incomes")
    public String index(Model model,
                        @RequestParam(required = false) String name,
                        @RequestParam(required = false) Long userId,
                        @RequestParam(required = false) Long categoryId,
                        @RequestParam(required = false) String startDate,
                        @RequestParam(required = false) String endDate,
                        @RequestParam(defaultValue = "0") Integer page,
                        @RequestParam(defaultValue = "20") Integer size) {
        AdminIncomeFilterRequest.AdminIncomeFilterRequestBuilder filterBuilder = AdminIncomeFilterRequest.builder()
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

        AdminIncomeFilterRequest filter = filterBuilder.build();
        PageResponse<IncomeResponse> response = adminIncomeService.getAllIncomes(filter);

        model.addAttribute("activeMenu", "incomes");
        model.addAttribute("incomes", response.getContent());
        model.addAttribute("currentPage", response.getPageNumber());
        model.addAttribute("totalPages", response.getTotalPages());
        model.addAttribute("totalElements", response.getTotalElements());
        model.addAttribute("filter", filter);

        return "admin/incomes/index";
    }

    @GetMapping("/incomes/{id}")
    public String detail(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("activeMenu", "incomes");
            IncomeResponse income = adminIncomeService.getIncomeById(id);
            model.addAttribute("income", income);
            return "admin/incomes/detail";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/incomes";
        }
    }

    @GetMapping("/incomes/{id}/edit")
    public String edit(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("activeMenu", "incomes");
            IncomeResponse income = adminIncomeService.getIncomeById(id);

            if (!model.containsAttribute("incomeRequest")) {
                IncomeRequest incomeRequest = new IncomeRequest();
                incomeRequest.setId(income.getId());
                incomeRequest.setName(income.getName());
                incomeRequest.setAmount(income.getAmount());
                incomeRequest.setIncomeDate(income.getIncomeDate());
                incomeRequest.setCategoryId(income.getCategoryId());
                incomeRequest.setNote(income.getNote());
                incomeRequest.setSource(income.getSource());
                incomeRequest.setIsRecurring(income.getIsRecurring());
                incomeRequest.setRecurringType(income.getRecurringType());
                model.addAttribute("incomeRequest", incomeRequest);
            }

            model.addAttribute("categories", categoryService.getActiveIncomeCategories());
            return "admin/incomes/form";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/incomes";
        }
    }

    @PutMapping("/incomes/{id}")
    public String update(@PathVariable Long id,
                        @Valid @ModelAttribute("incomeRequest") IncomeRequest incomeRequest,
                        BindingResult bindingResult,
                        Model model,
                        RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("activeMenu", "incomes");
            model.addAttribute("categories", categoryService.getActiveIncomeCategories());
            return "admin/incomes/form";
        }

        try {
            adminIncomeService.updateIncome(id, incomeRequest);
            redirectAttributes.addFlashAttribute("success",
                    messageSource.getMessage("admin.income.updated.success", null, LocaleContextHolder.getLocale()));
            return "redirect:/admin/incomes";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("incomeRequest", incomeRequest);
            return "redirect:/admin/incomes/" + id + "/edit";
        }
    }

    @DeleteMapping("/incomes/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            adminIncomeService.deleteIncome(id);
            redirectAttributes.addFlashAttribute("success",
                    messageSource.getMessage("admin.income.deleted.success", null, LocaleContextHolder.getLocale()));
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/incomes";
    }
}
