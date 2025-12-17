package com.sunasterisk.expense_management.repository.specification;

import com.sunasterisk.expense_management.entity.Budget;
import com.sunasterisk.expense_management.entity.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class BudgetSpecification {

    /**
     * Build dynamic query specification for Budget filtering
     *
     * @param user Current user
     * @param name Filter by budget name (partial match)
     * @param categoryId Filter by category ID
     * @param year Filter by year
     * @param month Filter by month
     * @param isOverBudget Filter by over budget status
     * @param active Filter by active status
     * @return Specification for Budget query
     */
    public static Specification<Budget> withFilters(
            User user,
            String name,
            Long categoryId,
            Integer year,
            Integer month,
            Boolean isOverBudget,
            Boolean active
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by user (required)
            if (user != null) {
                predicates.add(criteriaBuilder.equal(root.get("user"), user));
            }

            // Filter by name (case-insensitive partial match)
            if (name != null && !name.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        "%" + name.toLowerCase() + "%"
                ));
            }

            // Filter by category
            if (categoryId != null) {
                predicates.add(criteriaBuilder.equal(root.get("category").get("id"), categoryId));
            }

            // Filter by year
            if (year != null) {
                predicates.add(criteriaBuilder.equal(root.get("year"), year));
            }

            // Filter by month
            if (month != null) {
                predicates.add(criteriaBuilder.equal(root.get("month"), month));
            }

            // Filter by over budget status
            if (isOverBudget != null) {
                if (isOverBudget) {
                    // spentAmount > amountLimit
                    predicates.add(criteriaBuilder.greaterThan(
                            root.get("spentAmount"),
                            root.get("amountLimit")
                    ));
                } else {
                    // spentAmount <= amountLimit
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(
                            root.get("spentAmount"),
                            root.get("amountLimit")
                    ));
                }
            }

            // Filter by active status
            if (active != null) {
                predicates.add(criteriaBuilder.equal(root.get("active"), active));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
