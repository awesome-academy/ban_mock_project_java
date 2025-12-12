package com.sun.expense_management.mapper;

import com.sun.expense_management.dto.budget.BudgetRequest;
import com.sun.expense_management.dto.budget.BudgetResponse;
import com.sun.expense_management.entity.Budget;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface BudgetMapper {

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "categoryIcon", source = "category.icon")
    @Mapping(target = "categoryColor", source = "category.color")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", source = "user.name")
    @Mapping(target = "remainingAmount", expression = "java(budget.getRemainingAmount())")
    @Mapping(target = "usagePercentage", expression = "java(budget.getUsagePercentage())")
    @Mapping(target = "isOverBudget", expression = "java(budget.isOverBudget())")
    @Mapping(target = "shouldAlert", expression = "java(budget.shouldAlert())")
    BudgetResponse toResponse(Budget budget);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "spentAmount", ignore = true)
    @Mapping(target = "isAlertSent", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Budget toEntity(BudgetRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "spentAmount", ignore = true)
    @Mapping(target = "isAlertSent", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(BudgetRequest request, @MappingTarget Budget budget);
}
