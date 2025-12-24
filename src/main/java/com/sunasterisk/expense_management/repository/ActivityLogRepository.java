package com.sunasterisk.expense_management.repository;

import com.sunasterisk.expense_management.entity.ActivityLog;
import com.sunasterisk.expense_management.entity.ActivityLog.ActionType;
import com.sunasterisk.expense_management.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long>, JpaSpecificationExecutor<ActivityLog> {

    /**
     * Find all activity logs with eager loading of user relationship.
     */
    @EntityGraph(attributePaths = {"user"})
    Page<ActivityLog> findAll(Specification<ActivityLog> spec, Pageable pageable);

    /**
     * Find logs by user.
     */
    @EntityGraph(attributePaths = {"user"})
    Page<ActivityLog> findByUser(User user, Pageable pageable);

    /**
     * Find logs by action type.
     */
    Page<ActivityLog> findByAction(ActionType action, Pageable pageable);

    /**
     * Find logs by entity type.
     */
    Page<ActivityLog> findByEntityType(String entityType, Pageable pageable);

    /**
     * Find logs for a specific entity.
     */
    @EntityGraph(attributePaths = {"user"})
    List<ActivityLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, Long entityId);

    /**
     * Find logs within a date range.
     */
    @EntityGraph(attributePaths = {"user"})
    Page<ActivityLog> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Count logs by action type.
     */
    @Query("SELECT COUNT(a) FROM ActivityLog a WHERE a.action = :action")
    Long countByAction(@Param("action") ActionType action);

    /**
     * Delete old logs before a certain date.
     */
    void deleteByCreatedAtBefore(LocalDateTime date);

    /**
     * Find recent logs for a user.
     */
    @EntityGraph(attributePaths = {"user"})
    List<ActivityLog> findTop10ByUserOrderByCreatedAtDesc(User user);
}
