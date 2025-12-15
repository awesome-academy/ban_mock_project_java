package com.sunasterisk.expense_management.repository.specification;

import com.sunasterisk.expense_management.entity.Expense;
import com.sunasterisk.expense_management.entity.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Specification class for building dynamic queries for Expense entity.
 * Uses JPA Criteria API to construct type-safe, composable queries.
 *
 * Benefits:
 * - Type-safe: Compile-time checking of field names
 * - Composable: Can combine multiple specifications with and()/or()
 * - Flexible: Easy to add/remove filter conditions
 * - Maintainable: No long JPQL strings
 * - Reusable: Each method can be used independently
 */
public class ExpenseSpecification {

    /**
     * Filter by user (required for all queries)
     */
    public static Specification<Expense> hasUser(User user) {
        return (root, query, cb) -> cb.equal(root.get("user"), user);
    }

    /**
     * Filter by expense name (prefix matching for index optimization)
     */
    public static Specification<Expense> hasNameLike(String name) {
        return (root, query, cb) -> {
            if (name == null || name.trim().isEmpty()) {
                return cb.conjunction(); // Always true
            }
            return cb.like(root.get("name"), name + "%");
        };
    }

    /**
     * Filter by category ID
     */
    public static Specification<Expense> hasCategoryId(Long categoryId) {
        return (root, query, cb) -> {
            if (categoryId == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("category").get("id"), categoryId);
        };
    }

    /**
     * Filter by expense date >= startDate
     */
    public static Specification<Expense> hasExpenseDateFrom(LocalDate startDate) {
        return (root, query, cb) -> {
            if (startDate == null) {
                return cb.conjunction();
            }
            return cb.greaterThanOrEqualTo(root.get("expenseDate"), startDate);
        };
    }

    /**
     * Filter by expense date <= endDate
     */
    public static Specification<Expense> hasExpenseDateTo(LocalDate endDate) {
        return (root, query, cb) -> {
            if (endDate == null) {
                return cb.conjunction();
            }
            return cb.lessThanOrEqualTo(root.get("expenseDate"), endDate);
        };
    }

    /**
     * Filter by expense date between startDate and endDate
     */
    public static Specification<Expense> hasExpenseDateBetween(LocalDate startDate, LocalDate endDate) {
        return (root, query, cb) -> {
            if (startDate == null && endDate == null) {
                return cb.conjunction();
            }
            if (startDate == null) {
                return cb.lessThanOrEqualTo(root.get("expenseDate"), endDate);
            }
            if (endDate == null) {
                return cb.greaterThanOrEqualTo(root.get("expenseDate"), startDate);
            }
            return cb.between(root.get("expenseDate"), startDate, endDate);
        };
    }

    /**
     * Filter by amount >= minAmount
     */
    public static Specification<Expense> hasMinAmount(BigDecimal minAmount) {
        return (root, query, cb) -> {
            if (minAmount == null) {
                return cb.conjunction();
            }
            return cb.greaterThanOrEqualTo(root.get("amount"), minAmount);
        };
    }

    /**
     * Filter by amount <= maxAmount
     */
    public static Specification<Expense> hasMaxAmount(BigDecimal maxAmount) {
        return (root, query, cb) -> {
            if (maxAmount == null) {
                return cb.conjunction();
            }
            return cb.lessThanOrEqualTo(root.get("amount"), maxAmount);
        };
    }

    /**
     * Filter by amount between minAmount and maxAmount
     */
    public static Specification<Expense> hasAmountBetween(BigDecimal minAmount, BigDecimal maxAmount) {
        return (root, query, cb) -> {
            if (minAmount == null && maxAmount == null) {
                return cb.conjunction();
            }
            if (minAmount == null) {
                return cb.lessThanOrEqualTo(root.get("amount"), maxAmount);
            }
            if (maxAmount == null) {
                return cb.greaterThanOrEqualTo(root.get("amount"), minAmount);
            }
            return cb.between(root.get("amount"), minAmount, maxAmount);
        };
    }

    /**
     * Combine all filters for flexible querying.
     * This is a convenience method that combines all common filters.
     *
     * Usage example:
     * <pre>
     * Specification<Expense> spec = ExpenseSpecification.withFilters(
     *     user, "food", 1L, startDate, endDate, minAmount, maxAmount
     * );
     * Page<Expense> results = expenseRepository.findAll(spec, pageable);
     * </pre>
     */
    public static Specification<Expense> withFilters(
            User user,
            String name,
            Long categoryId,
            LocalDate startDate,
            LocalDate endDate,
            BigDecimal minAmount,
            BigDecimal maxAmount
    ) {
        return Specification.where(hasUser(user))
                .and(hasNameLike(name))
                .and(hasCategoryId(categoryId))
                .and(hasExpenseDateFrom(startDate))
                .and(hasExpenseDateTo(endDate))
                .and(hasMinAmount(minAmount))
                .and(hasMaxAmount(maxAmount));
    }

    /**
     * Alternative approach using dynamic predicates.
     * More flexible but slightly more verbose at call site.
     */
    public static Specification<Expense> buildDynamicQuery(
            User user,
            String name,
            Long categoryId,
            LocalDate startDate,
            LocalDate endDate,
            BigDecimal minAmount,
            BigDecimal maxAmount
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // User is always required
            predicates.add(cb.equal(root.get("user"), user));

            // Add optional filters only if provided
            if (name != null && !name.trim().isEmpty()) {
                predicates.add(cb.like(root.get("name"), name + "%"));
            }

            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }

            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("expenseDate"), startDate));
            }

            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("expenseDate"), endDate));
            }

            if (minAmount != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("amount"), minAmount));
            }

            if (maxAmount != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("amount"), maxAmount));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
