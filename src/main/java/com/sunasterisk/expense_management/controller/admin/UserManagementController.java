package com.sunasterisk.expense_management.controller.admin;

import com.sunasterisk.expense_management.dto.UserDto;
import com.sunasterisk.expense_management.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for User Management in Admin Panel
 */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class UserManagementController {

    private final UserService userService;
    private final MessageSource messageSource;

    /**
     * List all users
     */
    @GetMapping("/users")
    public String index(Model model) {
        model.addAttribute("activeMenu", "users");
        model.addAttribute("users", userService.getAllUsers());
        return "admin/users/index";
    }

    /**
     * View user detail
     */
    @GetMapping("/users/{id}")
    public String detail(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("activeMenu", "users");
            UserDto user = userService.getUserById(id);
            model.addAttribute("user", user);
            return "admin/users/detail";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/users";
        }
    }

    /**
     * Show create user form
     */
    @GetMapping("/users/create")
    public String create(Model model) {
        model.addAttribute("activeMenu", "users");
        model.addAttribute("user", new UserDto());
        return "admin/users/form";
    }

    /**
     * Show edit user form
     */
    @GetMapping("/users/{id}/edit")
    public String edit(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("activeMenu", "users");
            UserDto user = userService.getUserById(id);
            model.addAttribute("user", user);
            return "admin/users/form";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/users";
        }
    }

    /**
     * Save user (create or update)
     */
    @PostMapping("/users/save")
    public String save(@ModelAttribute UserDto user, RedirectAttributes redirectAttributes) {
        try {
            if (user.getId() == null) {
                // Create new user
                userService.createUser(user);
                redirectAttributes.addFlashAttribute("success",
                        messageSource.getMessage("admin.user.created.success", null, LocaleContextHolder.getLocale()));
            } else {
                // Update existing user
                userService.updateUser(user.getId(), user);
                redirectAttributes.addFlashAttribute("success",
                        messageSource.getMessage("admin.user.updated.success", null, LocaleContextHolder.getLocale()));
            }
            return "redirect:/admin/users";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/users/create";
        }
    }

    /**
     * Delete user
     */
    @GetMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("success",
                    messageSource.getMessage("admin.user.deleted.success", null, LocaleContextHolder.getLocale()));
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }
}
