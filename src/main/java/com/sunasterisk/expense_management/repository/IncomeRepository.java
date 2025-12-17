package com.sunasterisk.expense_management.repository;

import com.sunasterisk.expense_management.entity.Income;
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
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface IncomeRepository extends JpaRepository<Income, Long>, JpaSpecificationExecutor<Income> {

    Page<Income> findByUser(User user, Pageable pageable);

    Optional<Income> findByIdAndUser(Long id, User user);

    // Old @Query method removed - now using Specification pattern
    // See IncomeSpecification.withFilters() for flexible dynamic queries

    @Query("SELECT SUM(i.amount) FROM Income i WHERE i.user = :user " +
           "AND i.incomeDate BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByUserAndDateBetween(
            @Param("user") User user,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Count incomes in date range
     */
    @Query("SELECT COUNT(i) FROM Income i WHERE i.user.id = :userId " +
           "AND i.incomeDate BETWEEN :startDate AND :endDate")
    Long countByUserAndDateBetween(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Sum incomes in date range
     */
    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM Income i " +
           "WHERE i.user.id = :userId " +
           "AND i.incomeDate BETWEEN :startDate AND :endDate")
    BigDecimal sumByUserAndDateBetween(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Group incomes by month for trend analysis
     */
    @Query("SELECT YEAR(i.incomeDate), MONTH(i.incomeDate), " +
           "COALESCE(SUM(i.amount), 0), COUNT(i) " +
           "FROM Income i " +
           "WHERE i.user.id = :userId " +
           "AND i.incomeDate BETWEEN :startDate AND :endDate " +
           "GROUP BY YEAR(i.incomeDate), MONTH(i.incomeDate) " +
           "ORDER BY YEAR(i.incomeDate), MONTH(i.incomeDate)")
    java.util.List<Object[]> groupByMonthAndDateBetween(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Group incomes by quarter for trend analysis
     */
    @Query("SELECT YEAR(i.incomeDate), QUARTER(i.incomeDate), " +
           "COALESCE(SUM(i.amount), 0), COUNT(i) " +
           "FROM Income i " +
           "WHERE i.user.id = :userId " +
           "AND i.incomeDate BETWEEN :startDate AND :endDate " +
           "GROUP BY YEAR(i.incomeDate), QUARTER(i.incomeDate) " +
           "ORDER BY YEAR(i.incomeDate), QUARTER(i.incomeDate)")
    java.util.List<Object[]> groupByQuarterAndDateBetween(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Group incomes by year for trend analysis
     */
    @Query("SELECT YEAR(i.incomeDate), COALESCE(SUM(i.amount), 0), COUNT(i) " +
           "FROM Income i " +
           "WHERE i.user.id = :userId " +
           "AND i.incomeDate BETWEEN :startDate AND :endDate " +
           "GROUP BY YEAR(i.incomeDate) " +
           "ORDER BY YEAR(i.incomeDate)")
    java.util.List<Object[]> groupByYearAndDateBetween(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM Income i")
    Optional<BigDecimal> sumAllIncomes();

    @Query("SELECT COUNT(i) FROM Income i WHERE i.createdAt BETWEEN :start AND :end")
    Long countIncomesBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
