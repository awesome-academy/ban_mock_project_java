package com.sun.expense_management.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.expense_management.dto.error.ErrorResponse;
import com.sun.expense_management.util.MessageUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final MessageUtil messageUtil;

    public JwtAuthenticationEntryPoint(MessageUtil messageUtil) {
        this.messageUtil = messageUtil;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        String message = (String) request.getAttribute("jwt_error");
        if (message == null) {
            message = messageUtil.getMessage("jwt.invalid");
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpServletResponse.SC_UNAUTHORIZED)
                .error("Unauthorized")
                .message(message)
                .path(request.getRequestURI())
                .build();

        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), errorResponse);
    }
}
