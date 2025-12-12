package com.sun.expense_management.controller;

import com.sun.expense_management.dto.report.*;
import com.sun.expense_management.service.ReportService;
import com.sun.expense_management.validation.ValidTrendPeriod;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST API for Report & Analytics
 */
@Validated
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * Get report by time period (month, quarter, year, custom)
     *
     * @param request Time range with startDate and endDate
     * @return Report with total expense, income, balance, counts, and averages
     */
    @PostMapping("/by-time")
    public ResponseEntity<ReportByTimeResponse> getReportByTime(
            @Valid @RequestBody TimeRangeRequest request) {
        return ResponseEntity.ok(reportService.getReportByTime(request));
    }

    /**
     * Get category distribution (pie chart data)
     *
     * @param request Time range filter
     * @return Expense distribution grouped by category with percentages
     */
    @PostMapping("/by-category")
    public ResponseEntity<CategoryDistributionResponse> getCategoryDistribution(
            @Valid @RequestBody TimeRangeRequest request) {
        return ResponseEntity.ok(reportService.getCategoryDistribution(request));
    }

    /**
     * Get income vs expense comparison
     *
     * @param request Time range filter
     * @return Comparison with balance, savings rate, and financial health
     */
    @PostMapping("/income-vs-expense")
    public ResponseEntity<IncomeVsExpenseResponse> getIncomeVsExpense(
            @Valid @RequestBody TimeRangeRequest request) {
        return ResponseEntity.ok(reportService.getIncomeVsExpense(request));
    }

    /**
     * Get trend analysis over time
     *
     * @param period MONTHLY, QUARTERLY, or YEARLY
     * @param request Time range filter
     * @return Trend data with change percentages and direction
     */
    @PostMapping("/trend")
    public ResponseEntity<TrendAnalysisResponse> getTrendAnalysis(
            @ValidTrendPeriod
            @RequestParam(defaultValue = "MONTHLY") String period,
            @Valid @RequestBody TimeRangeRequest request) {
        return ResponseEntity.ok(reportService.getTrendAnalysis(period, request));
    }
}
