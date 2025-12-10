package com.sun.expense_management.repository;

import com.sun.expense_management.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long>, JpaSpecificationExecutor<Budget> {

    Optional<Budget> findByIdAndUser_Id(Long id, Long userId);

    boolean existsByUser_IdAndCategory_IdAndYearAndMonth(Long userId, Long categoryId, Integer year, Integer month);

    Optional<Budget> findByUser_IdAndCategory_IdAndYearAndMonth(Long userId, Long categoryId, Integer year, Integer month);
}
