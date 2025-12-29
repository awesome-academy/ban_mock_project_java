package com.sunasterisk.expense_management.dto.activitylog;

import com.sunasterisk.expense_management.entity.ActivityLog.ActionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLogFilterRequest {

    private Long userId;
    private ActionType action;
    private String entityType;
    private LocalDate startDate;
    private LocalDate endDate;

    @Builder.Default
    private Integer page = 0;

    @Builder.Default
    private Integer size = 20;

    @Builder.Default
    private String sortBy = "createdAt";

    @Builder.Default
    private String sortDir = "desc";
}
