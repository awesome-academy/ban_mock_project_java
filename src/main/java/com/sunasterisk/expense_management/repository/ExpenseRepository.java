package com.sunasterisk.expense_management.repository;

import com.sunasterisk.expense_management.entity.Expense;
import com.sunasterisk.expense_management.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long>, JpaSpecificationExecutor<Expense> {

    Page<Expense> findByUser(User user, Pageable pageable);

    Optional<Expense> findByIdAndUser(Long id, User user);

    // Old @Query method removed - now using Specification pattern
    // See ExpenseSpecification.withFilters() for flexible dynamic queries

    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.user = :user " +
           "AND e.expenseDate BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByUserAndDateBetween(
            @Param("user") User user,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Calculate total spent amount for a specific category in a given year/month
     * Used for Budget tracking
     */
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e " +
           "WHERE e.user.id = :userId " +
           "AND (:categoryId IS NULL OR e.category.id = :categoryId) " +
           "AND YEAR(e.expenseDate) = :year " +
           "AND MONTH(e.expenseDate) = :month")
    BigDecimal sumByUserAndCategoryAndYearMonth(
            @Param("userId") Long userId,
            @Param("categoryId") Long categoryId,
            @Param("year") Integer year,
            @Param("month") Integer month
    );

    /**
     * Count expenses in date range
     */
    @Query("SELECT COUNT(e) FROM Expense e WHERE e.user.id = :userId " +
           "AND e.expenseDate BETWEEN :startDate AND :endDate")
    Long countByUserAndDateBetween(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Sum expenses in date range
     */
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e " +
           "WHERE e.user.id = :userId " +
           "AND e.expenseDate BETWEEN :startDate AND :endDate")
    BigDecimal sumByUserAndDateBetween(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Group expenses by category for distribution analysis
     */
    @Query("SELECT e.category.id, e.category.name, e.category.icon, e.category.color, " +
           "COALESCE(SUM(e.amount), 0), COUNT(e) " +
           "FROM Expense e " +
           "WHERE e.user.id = :userId " +
           "AND e.expenseDate BETWEEN :startDate AND :endDate " +
           "GROUP BY e.category.id, e.category.name, e.category.icon, e.category.color " +
           "ORDER BY SUM(e.amount) DESC")
    java.util.List<Object[]> groupByCategoryAndDateBetween(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Group expenses by month for trend analysis
     */
    @Query("SELECT YEAR(e.expenseDate), MONTH(e.expenseDate), " +
           "COALESCE(SUM(e.amount), 0), COUNT(e) " +
           "FROM Expense e " +
           "WHERE e.user.id = :userId " +
           "AND e.expenseDate BETWEEN :startDate AND :endDate " +
           "GROUP BY YEAR(e.expenseDate), MONTH(e.expenseDate) " +
           "ORDER BY YEAR(e.expenseDate), MONTH(e.expenseDate)")
    java.util.List<Object[]> groupByMonthAndDateBetween(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Group expenses by quarter for trend analysis
     */
    @Query("SELECT YEAR(e.expenseDate), QUARTER(e.expenseDate), " +
           "COALESCE(SUM(e.amount), 0), COUNT(e) " +
           "FROM Expense e " +
           "WHERE e.user.id = :userId " +
           "AND e.expenseDate BETWEEN :startDate AND :endDate " +
           "GROUP BY YEAR(e.expenseDate), QUARTER(e.expenseDate) " +
           "ORDER BY YEAR(e.expenseDate), QUARTER(e.expenseDate)")
    java.util.List<Object[]> groupByQuarterAndDateBetween(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Group expenses by year for trend analysis
     */
    @Query("SELECT YEAR(e.expenseDate), COALESCE(SUM(e.amount), 0), COUNT(e) " +
           "FROM Expense e " +
           "WHERE e.user.id = :userId " +
           "AND e.expenseDate BETWEEN :startDate AND :endDate " +
           "GROUP BY YEAR(e.expenseDate) " +
           "ORDER BY YEAR(e.expenseDate)")
    java.util.List<Object[]> groupByYearAndDateBetween(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
