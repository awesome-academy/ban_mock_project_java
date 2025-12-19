package com.sunasterisk.expense_management.repository;

import com.sunasterisk.expense_management.entity.BudgetTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetTemplateRepository extends JpaRepository<BudgetTemplate, Long>, JpaSpecificationExecutor<BudgetTemplate> {

    List<BudgetTemplate> findByActiveTrue();

    Optional<BudgetTemplate> findByIdAndActiveTrue(Long id);
}
