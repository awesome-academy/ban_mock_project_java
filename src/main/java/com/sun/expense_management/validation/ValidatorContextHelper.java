package com.sun.expense_management.validation;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

/**
 * Helper class to provide Spring beans to Jakarta Validators
 * Since validators cannot use constructor injection, this provides static access to Spring context
 */
@Component
public class ValidatorContextHelper {

    private static MessageSource messageSource;

    public ValidatorContextHelper(MessageSource messageSource) {
        ValidatorContextHelper.messageSource = messageSource;
    }

    /**
     * Get internationalized message by key
     *
     * @param key Message key
     * @return Localized message
     */
    public static String getMessage(String key) {
        if (messageSource == null) {
            return key; // Fallback to key if not initialized
        }
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }

    /**
     * Get internationalized message with parameters
     *
     * @param key    Message key
     * @param params Parameters for message placeholders
     * @return Localized message with parameters
     */
    public static String getMessage(String key, Object... params) {
        if (messageSource == null) {
            return key; // Fallback to key if not initialized
        }
        return messageSource.getMessage(key, params, LocaleContextHolder.getLocale());
    }
}
