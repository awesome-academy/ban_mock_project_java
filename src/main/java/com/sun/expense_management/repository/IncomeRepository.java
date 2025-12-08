package com.sun.expense_management.repository;

import com.sun.expense_management.entity.Income;
import com.sun.expense_management.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface IncomeRepository extends JpaRepository<Income, Long> {

    Page<Income> findByUser(User user, Pageable pageable);

    Optional<Income> findByIdAndUser(Long id, User user);

    // NOTE: Using wildcards on both sides of the search term (e.g., %term%) will cause full table scans and prevent index usage.
    // For better performance, use prefix matching (e.g., term%) and ensure appropriate indexes are created on the 'name' column.
    @Query("SELECT i FROM Income i WHERE i.user = :user " +
           "AND (:name IS NULL OR i.name LIKE CONCAT(:name, '%')) " +
           "AND (:categoryId IS NULL OR i.category.id = :categoryId) " +
           "AND (:startDate IS NULL OR i.incomeDate >= :startDate) " +
           "AND (:endDate IS NULL OR i.incomeDate <= :endDate) " +
           "AND (:minAmount IS NULL OR i.amount >= :minAmount) " +
           "AND (:maxAmount IS NULL OR i.amount <= :maxAmount)")
    Page<Income> findByUserWithFilters(
            @Param("user") User user,
            @Param("name") String name,
            @Param("categoryId") Long categoryId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount,
            Pageable pageable
    );

    @Query("SELECT SUM(i.amount) FROM Income i WHERE i.user = :user " +
           "AND i.incomeDate BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByUserAndDateBetween(
            @Param("user") User user,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
