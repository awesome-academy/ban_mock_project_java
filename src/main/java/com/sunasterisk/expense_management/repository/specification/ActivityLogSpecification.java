package com.sunasterisk.expense_management.repository.specification;

import com.sunasterisk.expense_management.entity.ActivityLog;
import com.sunasterisk.expense_management.entity.ActivityLog.ActionType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ActivityLogSpecification {

    public static Specification<ActivityLog> withFilters(
            Long userId,
            ActionType action,
            String entityType,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (userId != null) {
                predicates.add(criteriaBuilder.equal(root.get("user").get("id"), userId));
            }

            if (action != null) {
                predicates.add(criteriaBuilder.equal(root.get("action"), action));
            }

            if (entityType != null && !entityType.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("entityType"), entityType));
            }

            if (startDate != null) {
                LocalDateTime startDateTime = startDate.atStartOfDay();
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), startDateTime));
            }

            if (endDate != null) {
                LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), endDateTime));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
