package com.sunasterisk.expense_management.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;

/**
 * Locale resolver that supports:
 * - Query param via {@link org.springframework.web.servlet.i18n.LocaleChangeInterceptor} (stored in session)
 * - Accept-Language header (when session locale not set)
 * Default locale is Vietnamese.
 */
public class HeaderAwareSessionLocaleResolver extends SessionLocaleResolver {

    private static final Locale DEFAULT_LOCALE = Locale.forLanguageTag("vi");

    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        String acceptLanguage = request.getHeader("Accept-Language");
        if (acceptLanguage != null && !acceptLanguage.isBlank()) {
            Locale headerLocale = request.getLocale();

            // For API calls, always prefer Accept-Language over any session locale.
            // This avoids "sticky" session locale breaking API i18n expectations.
            String requestUri = request.getRequestURI();
            if (requestUri != null && requestUri.startsWith("/api")) {
                return normalizeSupportedLocale(headerLocale);
            }
        }

        // For non-API paths (e.g., admin UI), keep session-based locale preference.
        Locale sessionLocale = resolveLocaleFromSession(request);
        if (sessionLocale != null) {
            return normalizeSupportedLocale(sessionLocale);
        }

        if (acceptLanguage != null && !acceptLanguage.isBlank()) {
            return normalizeSupportedLocale(request.getLocale());
        }

        Locale configuredDefault = getDefaultLocale();
        return configuredDefault != null ? configuredDefault : DEFAULT_LOCALE;
    }

    private Locale resolveLocaleFromSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }

        Object value = session.getAttribute(LOCALE_SESSION_ATTRIBUTE_NAME);
        if (value instanceof Locale locale) {
            return locale;
        }
        if (value instanceof String localeTag) {
            return Locale.forLanguageTag(localeTag);
        }
        return null;
    }

    private Locale normalizeSupportedLocale(Locale locale) {
        if (locale == null) {
            return DEFAULT_LOCALE;
        }
        String language = locale.getLanguage();
        if ("en".equalsIgnoreCase(language)) {
            return Locale.ENGLISH;
        }
        if ("vi".equalsIgnoreCase(language)) {
            return DEFAULT_LOCALE;
        }
        return DEFAULT_LOCALE;
    }
}
