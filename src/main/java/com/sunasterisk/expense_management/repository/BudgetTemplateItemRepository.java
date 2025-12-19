package com.sunasterisk.expense_management.repository;

import com.sunasterisk.expense_management.entity.BudgetTemplateItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BudgetTemplateItemRepository extends JpaRepository<BudgetTemplateItem, Long> {

    List<BudgetTemplateItem> findByBudgetTemplateId(Long budgetTemplateId);

    void deleteByBudgetTemplateId(Long budgetTemplateId);
}
