package com.sunasterisk.expense_management.repository.specification;

import com.sunasterisk.expense_management.entity.User;
import com.sunasterisk.expense_management.entity.User.Role;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Specification class for building dynamic User queries
 */
public class UserSpecification {

    /**
     * Build dynamic query specification for User filtering
     *
     * @param name Filter by user name (partial match)
     * @param email Filter by email (partial match)
     * @param role Filter by role
     * @param active Filter by active status
     * @return Specification for User query
     */
    public static Specification<User> withFilters(
            String name,
            String email,
            Role role,
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

            // Filter by email (case-insensitive partial match)
            if (email != null && !email.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("email")),
                        "%" + email.toLowerCase() + "%"
                ));
            }

            // Filter by role
            if (role != null) {
                predicates.add(criteriaBuilder.equal(root.get("role"), role));
            }

            // Filter by active status
            if (active != null) {
                predicates.add(criteriaBuilder.equal(root.get("active"), active));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
