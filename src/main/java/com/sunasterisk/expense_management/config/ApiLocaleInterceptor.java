package com.sunasterisk.expense_management.config;

import java.util.List;
import java.util.Locale;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class ApiLocaleInterceptor implements HandlerInterceptor {

    private final AcceptHeaderLocaleResolver acceptResolver =
            new AcceptHeaderLocaleResolver();

    {
        acceptResolver.setDefaultLocale(Locale.forLanguageTag("vi"));
        acceptResolver.setSupportedLocales(List.of(
                Locale.forLanguageTag("en"),
                Locale.forLanguageTag("vi")
        ));
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {

        if (request.getRequestURI().startsWith("/api")) {
            Locale locale = acceptResolver.resolveLocale(request);
            LocaleContextHolder.setLocale(locale);
        }
        return true;
    }
}