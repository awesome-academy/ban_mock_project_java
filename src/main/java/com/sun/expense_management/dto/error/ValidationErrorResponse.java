package com.sun.expense_management.dto.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Validation error response with detailed field errors
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValidationErrorResponse {

    @Builder.Default
    private String timestamp = LocalDateTime.now().toString();

    private int status;

    private String error;

    /**
     * Map of field names to error messages
     * Example: {"email": "Email không hợp lệ", "password": "Mật khẩu không được để trống"}
     */
    private Map<String, String> messages;

    private String path;
}
