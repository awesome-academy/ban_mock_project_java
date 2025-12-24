package com.sunasterisk.expense_management.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Base controller for all admin controllers.
 * Provides common constants and utility methods to avoid code duplication.
 */
@Slf4j
@RequiredArgsConstructor
public abstract class BaseAdminController {

    protected final MessageSource messageSource;

    // Common view prefixes
    protected static final String ADMIN_VIEW_PREFIX = "admin/";
    protected static final String REDIRECT_PREFIX = "redirect:/admin/";

    // Common view suffixes
    protected static final String VIEW_INDEX = "/index";
    protected static final String VIEW_DETAIL = "/detail";
    protected static final String VIEW_FORM = "/form";

    /**
     * Build view path for the given module and view type.
     *
     * @param module   The module name (e.g., "expenses", "incomes", "categories")
     * @param viewType The view type (e.g., VIEW_INDEX, VIEW_DETAIL, VIEW_FORM)
     * @return The complete view path
     */
    protected String view(String module, String viewType) {
        return ADMIN_VIEW_PREFIX + module + viewType;
    }

    /**
     * Build view path for index page.
     *
     * @param module The module name
     * @return The complete view path for index
     */
    protected String viewIndex(String module) {
        return view(module, VIEW_INDEX);
    }

    /**
     * Build view path for detail page.
     *
     * @param module The module name
     * @return The complete view path for detail
     */
    protected String viewDetail(String module) {
        return view(module, VIEW_DETAIL);
    }

    /**
     * Build view path for form page.
     *
     * @param module The module name
     * @return The complete view path for form
     */
    protected String viewForm(String module) {
        return view(module, VIEW_FORM);
    }

    /**
     * Build redirect path to module index.
     *
     * @param module The module name
     * @return The complete redirect path
     */
    protected String redirectToIndex(String module) {
        return REDIRECT_PREFIX + module;
    }

    /**
     * Build redirect path to edit page.
     *
     * @param module The module name
     * @param id     The resource ID
     * @return The complete redirect path
     */
    protected String redirectToEdit(String module, Long id) {
        return REDIRECT_PREFIX + module + "/" + id + "/edit";
    }

    /**
     * Get localized message.
     *
     * @param key  The message key
     * @param args Optional arguments for the message
     * @return The localized message
     */
    protected String getMessage(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }

    /**
     * Safely parse a date string to LocalDate.
     * Returns null if the string is null, empty, or in invalid format.
     *
     * @param dateString The date string to parse (expected format: yyyy-MM-dd)
     * @return The parsed LocalDate or null if parsing fails
     */
    protected LocalDate parseLocalDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateString.trim());
        } catch (DateTimeParseException e) {
            log.warn("Invalid date format: {}. Expected format: yyyy-MM-dd", dateString, e);
            return null;
        }
    }

    /**
     * Safely parse a date string to LocalDate with custom format.
     * Returns null if the string is null, empty, or in invalid format.
     *
     * @param dateString The date string to parse
     * @param formatter  The DateTimeFormatter to use
     * @return The parsed LocalDate or null if parsing fails
     */
    protected LocalDate parseLocalDate(String dateString, DateTimeFormatter formatter) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateString.trim(), formatter);
        } catch (DateTimeParseException e) {
            log.warn("Invalid date format: {} for formatter: {}", dateString, formatter, e);
            return null;
        }
    }
}
