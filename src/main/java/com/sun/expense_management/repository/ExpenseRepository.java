package com.sun.expense_management.repository;

import com.sun.expense_management.entity.Expense;
import com.sun.expense_management.entity.User;
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
}
