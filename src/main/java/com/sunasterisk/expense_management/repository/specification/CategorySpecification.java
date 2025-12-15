package com.sunasterisk.expense_management.repository.specification;

import com.sunasterisk.expense_management.entity.Category;
import com.sunasterisk.expense_management.entity.Category.CategoryType;
import com.sunasterisk.expense_management.entity.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class CategorySpecification {

    /**
     * Build dynamic query specification for Category filtering
     *
     * @param user Current user (null for admin to see all)
     * @param name Filter by category name (partial match)
     * @param type Filter by category type (EXPENSE/INCOME)
     * @param active Filter by active status
     * @return Specification for Category query
     */
    public static Specification<Category> withFilters(
            User user,
            String name,
            CategoryType type,
            Boolean active
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by user (user's own categories + default categories)
            if (user != null) {
                Predicate userCategories = criteriaBuilder.equal(root.get("user"), user);
                Predicate defaultCategories = criteriaBuilder.equal(root.get("isDefault"), true);
                predicates.add(criteriaBuilder.or(userCategories, defaultCategories));
            }

            // Filter by name (case-insensitive partial match)
            if (name != null && !name.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        "%" + name.toLowerCase() + "%"
                ));
            }

            // Filter by type
            if (type != null) {
                predicates.add(criteriaBuilder.equal(root.get("type"), type));
            }

            // Filter by active status
            if (active != null) {
                predicates.add(criteriaBuilder.equal(root.get("active"), active));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
