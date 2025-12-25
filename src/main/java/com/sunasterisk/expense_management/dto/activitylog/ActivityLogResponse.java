package com.sunasterisk.expense_management.dto.activitylog;

import com.sunasterisk.expense_management.entity.ActivityLog.ActionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLogResponse {

    private Long id;
    private LocalDateTime createdAt;
    private ActionType action;
    private String entityType;
    private Long entityId;
    private String description;
    private String oldValue;
    private String newValue;
    private String ipAddress;
    private String userAgent;

    // User info
    private Long userId;
    private String userName;
    private String userEmail;
}
