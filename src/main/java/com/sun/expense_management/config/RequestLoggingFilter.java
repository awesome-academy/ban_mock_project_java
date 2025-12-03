package com.sun.expense_management.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();

        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            logRequest(requestWrapper, responseWrapper, duration);
            responseWrapper.copyBodyToResponse();
        }
    }

    private void logRequest(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response, long duration) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        int status = response.getStatus();

        StringBuilder logMessage = new StringBuilder();
        logMessage.append("\n========== HTTP Request ==========\n");
        logMessage.append(String.format("%-15s: %s %s\n", "Method", method, uri));

        if (queryString != null) {
            logMessage.append(String.format("%-15s: %s\n", "Query Params", queryString));
        }

        logMessage.append(String.format("%-15s: \n", "Headers"));
        Collections.list(request.getHeaderNames()).forEach(headerName -> {
            if (!headerName.equalsIgnoreCase("authorization")) {
                logMessage.append(String.format("  %s: %s\n", headerName, request.getHeader(headerName)));
            } else {
                logMessage.append(String.format("  %s: Bearer ***\n", headerName));
            }
        });

        if ("POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method)) {
            byte[] content = request.getContentAsByteArray();
            if (content.length > 0) {
                String body = new String(content, StandardCharsets.UTF_8);
                logMessage.append(String.format("%-15s: %s\n", "Request Body", body));
            }
        }

        logMessage.append("========== HTTP Response ==========\n");
        logMessage.append(String.format("%-15s: %d\n", "Status", status));
        logMessage.append(String.format("%-15s: %d ms\n", "Duration", duration));
        logMessage.append("===================================");

        if (status >= 400) {
            log.warn(logMessage.toString());
        } else {
            log.info(logMessage.toString());
        }
    }
}
