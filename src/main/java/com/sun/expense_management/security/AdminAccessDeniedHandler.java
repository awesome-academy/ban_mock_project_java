package com.sun.expense_management.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Custom AccessDeniedHandler for admin endpoints
 * Redirects to login page for admin paths, returns JSON for API paths
 */
@Component
public class AdminAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {

        String requestPath = request.getRequestURI();

        // For admin paths, redirect to login page
        if (requestPath.startsWith("/admin")) {
            // Save current URL as redirect parameter
            String redirectUrl = requestPath;
            if (request.getQueryString() != null) {
                redirectUrl += "?" + request.getQueryString();
            }
            response.sendRedirect("/admin/login?error=access_denied&redirect=" +
                java.net.URLEncoder.encode(redirectUrl, "UTF-8"));
            return;
        }

        // For API paths, return 403 Forbidden (will be handled by GlobalExceptionHandler)
        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
    }
}
