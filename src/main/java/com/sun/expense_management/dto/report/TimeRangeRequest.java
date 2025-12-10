package com.sun.expense_management.dto.report;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO for time range filter in reports
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeRangeRequest {

    @NotNull(message = "{report.start.date.required}")
    private LocalDate startDate;

    @NotNull(message = "{report.end.date.required}")
    private LocalDate endDate;

    private Long categoryId; // Optional: filter by specific category
}
