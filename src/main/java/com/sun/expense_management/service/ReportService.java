package com.sun.expense_management.service;

import com.sun.expense_management.dto.report.*;
import com.sun.expense_management.entity.User;
import com.sun.expense_management.exception.ResourceNotFoundException;
import com.sun.expense_management.repository.ExpenseRepository;
import com.sun.expense_management.repository.IncomeRepository;
import com.sun.expense_management.repository.UserRepository;
import com.sun.expense_management.util.MessageUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReportService {

    private final ExpenseRepository expenseRepository;
    private final IncomeRepository incomeRepository;
    private final UserRepository userRepository;
    private final MessageUtil messageUtil;

    public ReportService(ExpenseRepository expenseRepository,
                        IncomeRepository incomeRepository,
                        UserRepository userRepository,
                        MessageUtil messageUtil) {
        this.expenseRepository = expenseRepository;
        this.incomeRepository = incomeRepository;
        this.userRepository = userRepository;
        this.messageUtil = messageUtil;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(messageUtil.getMessage("user.not.found")));
    }

    /**
     * Get report by time period
     */
    @Transactional(readOnly = true)
    public ReportByTimeResponse getReportByTime(TimeRangeRequest request) {
        User user = getCurrentUser();

        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();

        // Calculate totals
        BigDecimal totalExpense = expenseRepository.sumByUserAndDateBetween(
                user.getId(), startDate, endDate);
        BigDecimal totalIncome = incomeRepository.sumByUserAndDateBetween(
                user.getId(), startDate, endDate);

        Long expenseCount = expenseRepository.countByUserAndDateBetween(
                user.getId(), startDate, endDate);
        Long incomeCount = incomeRepository.countByUserAndDateBetween(
                user.getId(), startDate, endDate);

        // Calculate averages
        BigDecimal averageExpense = expenseCount > 0
                ? totalExpense.divide(BigDecimal.valueOf(expenseCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        BigDecimal averageIncome = incomeCount > 0
                ? totalIncome.divide(BigDecimal.valueOf(incomeCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Determine period type
        String period = determinePeriodType(startDate, endDate);

        return ReportByTimeResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalExpense(totalExpense)
                .totalIncome(totalIncome)
                .balance(totalIncome.subtract(totalExpense))
                .expenseCount(expenseCount)
                .incomeCount(incomeCount)
                .averageExpense(averageExpense)
                .averageIncome(averageIncome)
                .period(period)
                .build();
    }

    /**
     * Get category distribution
     */
    @Transactional(readOnly = true)
    public CategoryDistributionResponse getCategoryDistribution(TimeRangeRequest request) {
        User user = getCurrentUser();

        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();

        BigDecimal totalExpense = expenseRepository.sumByUserAndDateBetween(
                user.getId(), startDate, endDate);

        List<Object[]> categoryData = expenseRepository.groupByCategoryAndDateBetween(
                user.getId(), startDate, endDate);

        List<CategoryDistributionResponse.CategoryItem> categories = new ArrayList<>();

        for (Object[] row : categoryData) {
            Long categoryId = ((Number) row[0]).longValue();
            String categoryName = (String) row[1];
            String categoryIcon = (String) row[2];
            String categoryColor = (String) row[3];
            BigDecimal amount = (BigDecimal) row[4];
            Long count = ((Number) row[5]).longValue();

            Double percentage = totalExpense.compareTo(BigDecimal.ZERO) > 0
                    ? amount.divide(totalExpense, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .doubleValue()
                    : 0.0;

            categories.add(CategoryDistributionResponse.CategoryItem.builder()
                    .categoryId(categoryId)
                    .categoryName(categoryName)
                    .categoryIcon(categoryIcon)
                    .categoryColor(categoryColor)
                    .amount(amount)
                    .count(count)
                    .percentage(percentage)
                    .build());
        }

        return CategoryDistributionResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalExpense(totalExpense)
                .categories(categories)
                .build();
    }

    /**
     * Get income vs expense comparison
     */
    @Transactional(readOnly = true)
    public IncomeVsExpenseResponse getIncomeVsExpense(TimeRangeRequest request) {
        User user = getCurrentUser();

        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();

        // Income data
        BigDecimal totalIncome = incomeRepository.sumByUserAndDateBetween(
                user.getId(), startDate, endDate);
        Long incomeCount = incomeRepository.countByUserAndDateBetween(
                user.getId(), startDate, endDate);
        BigDecimal averageIncome = incomeCount > 0
                ? totalIncome.divide(BigDecimal.valueOf(incomeCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Expense data
        BigDecimal totalExpense = expenseRepository.sumByUserAndDateBetween(
                user.getId(), startDate, endDate);
        Long expenseCount = expenseRepository.countByUserAndDateBetween(
                user.getId(), startDate, endDate);
        BigDecimal averageExpense = expenseCount > 0
                ? totalExpense.divide(BigDecimal.valueOf(expenseCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Comparison
        BigDecimal balance = totalIncome.subtract(totalExpense);
        BigDecimal savingsRate = totalIncome.compareTo(BigDecimal.ZERO) > 0
                ? balance.divide(totalIncome, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        String financialHealth = determineFinancialHealth(balance, totalIncome);

        return IncomeVsExpenseResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalIncome(totalIncome)
                .incomeCount(incomeCount)
                .averageIncome(averageIncome)
                .totalExpense(totalExpense)
                .expenseCount(expenseCount)
                .averageExpense(averageExpense)
                .balance(balance)
                .savingsRate(savingsRate)
                .financialHealth(financialHealth)
                .build();
    }

    /**
     * Get trend analysis
     *
     * Note: period parameter is validated at controller layer with @ValidTrendPeriod
     * Valid values: MONTHLY, QUARTERLY, YEARLY
     */
    @Transactional(readOnly = true)
    public TrendAnalysisResponse getTrendAnalysis(String period, TimeRangeRequest request) {
        User user = getCurrentUser();

        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();

        List<TrendAnalysisResponse.TrendItem> trends;

        // Period is already validated at controller layer, safe to use switch
        switch (period.toUpperCase()) {
            case "MONTHLY":
                trends = getMonthlyTrends(user.getId(), startDate, endDate);
                break;
            case "QUARTERLY":
                trends = getQuarterlyTrends(user.getId(), startDate, endDate);
                break;
            case "YEARLY":
                trends = getYearlyTrends(user.getId(), startDate, endDate);
                break;
            default:
                // Should never reach here due to controller validation
                throw new IllegalStateException("Invalid period: " + period);
        }

        // Calculate statistics
        BigDecimal averageExpense = calculateAverageExpense(trends);
        BigDecimal maxExpense = calculateMaxExpense(trends);
        BigDecimal minExpense = calculateMinExpense(trends);
        String trendDirection = determineTrendDirection(trends);

        return TrendAnalysisResponse.builder()
                .period(period.toUpperCase())
                .trends(trends)
                .averageExpense(averageExpense)
                .maxExpense(maxExpense)
                .minExpense(minExpense)
                .trendDirection(trendDirection)
                .build();
    }

    private List<TrendAnalysisResponse.TrendItem> getMonthlyTrends(
            Long userId, LocalDate startDate, LocalDate endDate) {

        List<Object[]> expenseData = expenseRepository.groupByMonthAndDateBetween(
                userId, startDate, endDate);
        List<Object[]> incomeData = incomeRepository.groupByMonthAndDateBetween(
                userId, startDate, endDate);

        return mergeTrendData(expenseData, incomeData, "MONTHLY");
    }

    private List<TrendAnalysisResponse.TrendItem> getQuarterlyTrends(
            Long userId, LocalDate startDate, LocalDate endDate) {

        List<Object[]> expenseData = expenseRepository.groupByQuarterAndDateBetween(
                userId, startDate, endDate);
        List<Object[]> incomeData = incomeRepository.groupByQuarterAndDateBetween(
                userId, startDate, endDate);

        return mergeTrendData(expenseData, incomeData, "QUARTERLY");
    }

    private List<TrendAnalysisResponse.TrendItem> getYearlyTrends(
            Long userId, LocalDate startDate, LocalDate endDate) {

        List<Object[]> expenseData = expenseRepository.groupByYearAndDateBetween(
                userId, startDate, endDate);
        List<Object[]> incomeData = incomeRepository.groupByYearAndDateBetween(
                userId, startDate, endDate);

        return mergeTrendData(expenseData, incomeData, "YEARLY");
    }

    private List<TrendAnalysisResponse.TrendItem> mergeTrendData(
            List<Object[]> expenseData, List<Object[]> incomeData, String periodType) {

        List<TrendAnalysisResponse.TrendItem> trends = new ArrayList<>();

        // Create map for income data
        java.util.Map<String, Object[]> incomeMap = new java.util.HashMap<>();
        for (Object[] row : incomeData) {
            String key = createPeriodKey(row, periodType);
            incomeMap.put(key, row);
        }

        BigDecimal previousExpense = null;

        for (Object[] expenseRow : expenseData) {
            Integer year = (Integer) expenseRow[0];
            Integer monthOrQuarter = periodType.equals("YEARLY") ? null : (Integer) expenseRow[1];

            BigDecimal totalExpense = periodType.equals("YEARLY")
                    ? (BigDecimal) expenseRow[1]
                    : (BigDecimal) expenseRow[2];
            Long expenseCount = periodType.equals("YEARLY")
                    ? ((Number) expenseRow[2]).longValue()
                    : ((Number) expenseRow[3]).longValue();

            // Get income data
            String key = createPeriodKey(expenseRow, periodType);
            Object[] incomeRow = incomeMap.get(key);

            BigDecimal totalIncome = BigDecimal.ZERO;
            Long incomeCount = 0L;

            if (incomeRow != null) {
                totalIncome = periodType.equals("YEARLY")
                        ? (BigDecimal) incomeRow[1]
                        : (BigDecimal) incomeRow[2];
                incomeCount = periodType.equals("YEARLY")
                        ? ((Number) incomeRow[2]).longValue()
                        : ((Number) incomeRow[3]).longValue();
            }

            // Calculate change percentage
            Double changePercentage = null;
            if (previousExpense != null && previousExpense.compareTo(BigDecimal.ZERO) > 0) {
                changePercentage = totalExpense.subtract(previousExpense)
                        .divide(previousExpense, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .doubleValue();
            }

            String periodStr = formatPeriod(year, monthOrQuarter, periodType);

            trends.add(TrendAnalysisResponse.TrendItem.builder()
                    .period(periodStr)
                    .year(year)
                    .month(periodType.equals("MONTHLY") ? monthOrQuarter : null)
                    .quarter(periodType.equals("QUARTERLY") ? monthOrQuarter : null)
                    .totalExpense(totalExpense)
                    .totalIncome(totalIncome)
                    .balance(totalIncome.subtract(totalExpense))
                    .expenseCount(expenseCount)
                    .incomeCount(incomeCount)
                    .changePercentage(changePercentage)
                    .build());

            previousExpense = totalExpense;
        }

        return trends;
    }

    private String createPeriodKey(Object[] row, String periodType) {
        Integer year = (Integer) row[0];
        if (periodType.equals("YEARLY")) {
            return String.valueOf(year);
        }
        Integer monthOrQuarter = (Integer) row[1];
        return year + "-" + monthOrQuarter;
    }

    private String formatPeriod(Integer year, Integer monthOrQuarter, String periodType) {
        if (periodType.equals("YEARLY")) {
            return String.valueOf(year);
        }
        if (periodType.equals("QUARTERLY")) {
            return year + "-Q" + monthOrQuarter;
        }
        return year + "-" + String.format("%02d", monthOrQuarter);
    }

    private String determinePeriodType(LocalDate startDate, LocalDate endDate) {
        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;

        if (days <= 31) {
            return "month";
        } else if (days <= 92) {
            return "quarter";
        } else if (days <= 366) {
            return "year";
        }
        return "custom";
    }

    private String determineFinancialHealth(BigDecimal balance, BigDecimal totalIncome) {
        if (totalIncome.compareTo(BigDecimal.ZERO) == 0) {
            return "UNKNOWN";
        }

        BigDecimal ratio = balance.divide(totalIncome, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        if (ratio.compareTo(BigDecimal.valueOf(10)) >= 0) {
            return "SURPLUS";
        } else if (ratio.compareTo(BigDecimal.valueOf(-10)) <= 0) {
            return "DEFICIT";
        }
        return "BALANCED";
    }

    private BigDecimal calculateAverageExpense(List<TrendAnalysisResponse.TrendItem> trends) {
        if (trends.isEmpty()) return BigDecimal.ZERO;

        BigDecimal sum = trends.stream()
                .map(TrendAnalysisResponse.TrendItem::getTotalExpense)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return sum.divide(BigDecimal.valueOf(trends.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateMaxExpense(List<TrendAnalysisResponse.TrendItem> trends) {
        return trends.stream()
                .map(TrendAnalysisResponse.TrendItem::getTotalExpense)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }

    private BigDecimal calculateMinExpense(List<TrendAnalysisResponse.TrendItem> trends) {
        return trends.stream()
                .map(TrendAnalysisResponse.TrendItem::getTotalExpense)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }

    private String determineTrendDirection(List<TrendAnalysisResponse.TrendItem> trends) {
        if (trends.size() < 2) return "STABLE";

        // Compare first half vs second half
        int mid = trends.size() / 2;
        BigDecimal firstHalfAvg = trends.subList(0, mid).stream()
                .map(TrendAnalysisResponse.TrendItem::getTotalExpense)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(mid), 2, RoundingMode.HALF_UP);

        BigDecimal secondHalfAvg = trends.subList(mid, trends.size()).stream()
                .map(TrendAnalysisResponse.TrendItem::getTotalExpense)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(trends.size() - mid), 2, RoundingMode.HALF_UP);

        BigDecimal diff = secondHalfAvg.subtract(firstHalfAvg);
        BigDecimal threshold = firstHalfAvg.multiply(BigDecimal.valueOf(0.1)); // 10% threshold

        if (diff.compareTo(threshold) > 0) {
            return "INCREASING";
        } else if (diff.compareTo(threshold.negate()) < 0) {
            return "DECREASING";
        }
        return "STABLE";
    }
}
