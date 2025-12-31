package com.sunasterisk.expense_management.controller.admin;

import com.sunasterisk.expense_management.dto.PageResponse;
import com.sunasterisk.expense_management.dto.UserDto;
import com.sunasterisk.expense_management.dto.csv.CsvImportResult;
import com.sunasterisk.expense_management.dto.user.AdminUserFilterRequest;
import com.sunasterisk.expense_management.entity.User.Role;
import com.sunasterisk.expense_management.service.CsvExportService;
import com.sunasterisk.expense_management.service.CsvImportService;
import com.sunasterisk.expense_management.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for User Management in Admin Panel
 */
@Controller
@RequestMapping("/admin")
public class AdminUserController extends BaseAdminController {

    private static final String MODULE = "users";

    private final UserService userService;
    private final CsvExportService csvExportService;
    private final CsvImportService csvImportService;

    public AdminUserController(UserService userService,
                                    CsvExportService csvExportService,
                                    CsvImportService csvImportService,
                                    MessageSource messageSource) {
        super(messageSource);
        this.userService = userService;
        this.csvExportService = csvExportService;
        this.csvImportService = csvImportService;
    }

    /**
     * List all users with pagination and filtering
     */
    @GetMapping("/users")
    public String index(Model model,
                        @RequestParam(required = false) String name,
                        @RequestParam(required = false) String email,
                        @RequestParam(required = false) Role role,
                        @RequestParam(required = false) Boolean active,
                        @RequestParam(defaultValue = "0") Integer page,
                        @RequestParam(defaultValue = "20") Integer size) {

        AdminUserFilterRequest filter = AdminUserFilterRequest.builder()
                .name(name)
                .email(email)
                .role(role)
                .active(active)
                .page(page)
                .size(size)
                .build();

        PageResponse<UserDto> response = userService.getAllUsers(filter);

        model.addAttribute("activeMenu", MODULE);
        model.addAttribute("users", response.getContent());
        model.addAttribute("currentPage", response.getPageNumber());
        model.addAttribute("totalPages", response.getTotalPages());
        model.addAttribute("totalElements", response.getTotalElements());
        model.addAttribute("filter", filter);
        model.addAttribute("roles", Role.values());

        return viewIndex(MODULE);
    }

    /**
     * View user detail
     */
    @GetMapping("/users/{id}")
    public String detail(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("activeMenu", MODULE);
            UserDto user = userService.getUserById(id);
            model.addAttribute("user", user);
            return viewDetail(MODULE);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return redirectToIndex(MODULE);
        }
    }

    /**
     * Show create user form
     */
    @GetMapping("/users/new")
    public String newUser(Model model) {
        model.addAttribute("activeMenu", MODULE);
        model.addAttribute("roles", Role.values());
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new UserDto());
        }
        return viewForm(MODULE);
    }

    /**
     * Show edit user form
     */
    @GetMapping("/users/{id}/edit")
    public String edit(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("activeMenu", MODULE);
            model.addAttribute("roles", Role.values());
            if (!model.containsAttribute("user")) {
                UserDto user = userService.getUserById(id);
                model.addAttribute("user", user);
            }
            return viewForm(MODULE);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return redirectToIndex(MODULE);
        }
    }

    /**
     * Create new user
     */
    @PostMapping("/users")
    public String create(@Valid @ModelAttribute("user") UserDto user, BindingResult bindingResult,
                        Model model, RedirectAttributes redirectAttributes) {
        // Check email exists
        if (userService.existsByEmail(user.getEmail())) {
            bindingResult.rejectValue("email", "admin.user.email.exists",
                    getMessage("admin.user.email.exists"));
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("activeMenu", MODULE);
            model.addAttribute("roles", Role.values());
            return viewForm(MODULE);
        }
        try {
            userService.createUser(user);
            redirectAttributes.addFlashAttribute("success", getMessage("admin.user.created.success"));
            return redirectToIndex(MODULE);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("user", user);
            return "redirect:/admin/users/new";
        }
    }

    /**
     * Update existing user
     */
    @PutMapping("/users/{id}")
    public String update(@PathVariable("id") Long id, @Valid @ModelAttribute("user") UserDto user,
                        BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        // Check email exists (except current user)
        UserDto existingUser = userService.getUserById(id);
        if (!existingUser.getEmail().equals(user.getEmail()) && userService.existsByEmail(user.getEmail())) {
            bindingResult.rejectValue("email", "admin.user.email.exists",
                    getMessage("admin.user.email.exists"));
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("activeMenu", MODULE);
            model.addAttribute("roles", Role.values());
            user.setId(id);
            return viewForm(MODULE);
        }
        try {
            userService.updateUser(id, user);
            redirectAttributes.addFlashAttribute("success", getMessage("admin.user.updated.success"));
            return redirectToIndex(MODULE);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("user", user);
            return "redirect:/admin/users/" + id + "/edit";
        }
    }

    /**
     * Delete user
     */
    @DeleteMapping("/users/{id}")
    public String deleteUser(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("success", getMessage("admin.user.deleted.success"));
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return redirectToIndex(MODULE);
    }

    /**
     * Export users to CSV
     */
    @GetMapping("/users/export")
    public void exportUsers(HttpServletResponse response) {
        try {
            csvExportService.exportUsers(response);
        } catch (Exception e) {
            throw new RuntimeException(getMessage("admin.user.export.failed") + ": " + e.getMessage(), e);
        }
    }

    /**
     * Import users from CSV
     */
    @PostMapping("/users/import")
    public String importUsers(@RequestParam("file") MultipartFile file,
                            RedirectAttributes redirectAttributes) {
        try {
            CsvImportResult result = csvImportService.importUsers(file);
            redirectAttributes.addFlashAttribute("importResult", result);

            if (!result.hasErrors()) {
                redirectAttributes.addFlashAttribute("success",
                        getMessage("admin.user.import.success", result.getSuccessCount()));
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    getMessage("admin.user.import.failed") + ": " + e.getMessage());
        }
        return redirectToIndex(MODULE);
    }
}
