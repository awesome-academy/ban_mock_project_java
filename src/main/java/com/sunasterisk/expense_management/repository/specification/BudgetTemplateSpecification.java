package com.sunasterisk.expense_management.repository.specification;

import com.sunasterisk.expense_management.entity.BudgetTemplate;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class BudgetTemplateSpecification {

    /**
     * Build dynamic query specification for BudgetTemplate filtering
     *
     * @param name Filter by template name (partial match)
     * @param active Filter by active status
     * @return Specification for BudgetTemplate query
     */
    public static Specification<BudgetTemplate> withFilters(
            String name,
            Boolean active
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by name (case-insensitive partial match)
            if (name != null && !name.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        "%" + name.toLowerCase() + "%"
                ));
            }

            // Filter by active status
            if (active != null) {
                predicates.add(criteriaBuilder.equal(root.get("active"), active));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
