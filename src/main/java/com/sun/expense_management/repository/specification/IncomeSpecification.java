package com.sun.expense_management.repository.specification;

import com.sun.expense_management.entity.Income;
import com.sun.expense_management.entity.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Specification class for building dynamic queries for Income entity.
 * Uses JPA Criteria API to construct type-safe, composable queries.
 *
 * Benefits:
 * - Type-safe: Compile-time checking of field names
 * - Composable: Can combine multiple specifications with and()/or()
 * - Flexible: Easy to add/remove filter conditions
 * - Maintainable: No long JPQL strings
 * - Reusable: Each method can be used independently
 */
public class IncomeSpecification {

    /**
     * Filter by user (required for all queries)
     */
    public static Specification<Income> hasUser(User user) {
        return (root, query, cb) -> cb.equal(root.get("user"), user);
    }

    /**
     * Filter by income name (prefix matching for index optimization)
     */
    public static Specification<Income> hasNameLike(String name) {
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
    public static Specification<Income> hasCategoryId(Long categoryId) {
        return (root, query, cb) -> {
            if (categoryId == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("category").get("id"), categoryId);
        };
    }

    /**
     * Filter by income date >= startDate
     */
    public static Specification<Income> hasIncomeDateFrom(LocalDate startDate) {
        return (root, query, cb) -> {
            if (startDate == null) {
                return cb.conjunction();
            }
            return cb.greaterThanOrEqualTo(root.get("incomeDate"), startDate);
        };
    }

    /**
     * Filter by income date <= endDate
     */
    public static Specification<Income> hasIncomeDateTo(LocalDate endDate) {
        return (root, query, cb) -> {
            if (endDate == null) {
                return cb.conjunction();
            }
            return cb.lessThanOrEqualTo(root.get("incomeDate"), endDate);
        };
    }

    /**
     * Filter by income date between startDate and endDate
     */
    public static Specification<Income> hasIncomeDateBetween(LocalDate startDate, LocalDate endDate) {
        return (root, query, cb) -> {
            if (startDate == null && endDate == null) {
                return cb.conjunction();
            }
            if (startDate == null) {
                return cb.lessThanOrEqualTo(root.get("incomeDate"), endDate);
            }
            if (endDate == null) {
                return cb.greaterThanOrEqualTo(root.get("incomeDate"), startDate);
            }
            return cb.between(root.get("incomeDate"), startDate, endDate);
        };
    }

    /**
     * Filter by amount >= minAmount
     */
    public static Specification<Income> hasMinAmount(BigDecimal minAmount) {
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
    public static Specification<Income> hasMaxAmount(BigDecimal maxAmount) {
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
    public static Specification<Income> hasAmountBetween(BigDecimal minAmount, BigDecimal maxAmount) {
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
     * Specification<Income> spec = IncomeSpecification.withFilters(
     *     user, "salary", 1L, startDate, endDate, minAmount, maxAmount
     * );
     * Page<Income> results = incomeRepository.findAll(spec, pageable);
     * </pre>
     */
    public static Specification<Income> withFilters(
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
                .and(hasIncomeDateFrom(startDate))
                .and(hasIncomeDateTo(endDate))
                .and(hasMinAmount(minAmount))
                .and(hasMaxAmount(maxAmount));
    }

    /**
     * Alternative approach using dynamic predicates.
     * More flexible but slightly more verbose at call site.
     */
    public static Specification<Income> buildDynamicQuery(
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
                predicates.add(cb.greaterThanOrEqualTo(root.get("incomeDate"), startDate));
            }

            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("incomeDate"), endDate));
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
