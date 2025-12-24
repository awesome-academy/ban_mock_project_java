package com.sunasterisk.expense_management.service;

import com.sunasterisk.expense_management.dto.PageResponse;
import com.sunasterisk.expense_management.dto.activitylog.ActivityLogFilterRequest;
import com.sunasterisk.expense_management.dto.activitylog.ActivityLogResponse;
import com.sunasterisk.expense_management.entity.ActivityLog;
import com.sunasterisk.expense_management.entity.ActivityLog.ActionType;
import com.sunasterisk.expense_management.entity.User;
import com.sunasterisk.expense_management.mapper.ActivityLogMapper;
import com.sunasterisk.expense_management.repository.ActivityLogRepository;
import com.sunasterisk.expense_management.repository.specification.ActivityLogSpecification;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final ActivityLogMapper activityLogMapper;

    /**
     * Log an activity asynchronously.
     */
    @Async
    @Transactional
    public void log(ActionType action, User user, String entityType, Long entityId, String description) {
        try {
            String ipAddress = getClientIp();

            ActivityLog activityLog = ActivityLog.builder()
                    .action(action)
                    .user(user)
                    .entityType(entityType)
                    .entityId(entityId)
                    .description(description)
                    .ipAddress(ipAddress)
                    .build();

            activityLogRepository.save(activityLog);
            log.debug("Activity logged: {} - {} - {}", action, entityType, description);
        } catch (Exception e) {
            log.error("Failed to log activity: {}", e.getMessage(), e);
        }
    }

    /**
     * Log an activity with old and new values.
     */
    @Async
    @Transactional
    public void logWithValues(ActionType action, User user, String entityType, Long entityId,
                              String description, String oldValue, String newValue) {
        try {
            String ipAddress = getClientIp();

            ActivityLog activityLog = ActivityLog.builder()
                    .action(action)
                    .user(user)
                    .entityType(entityType)
                    .entityId(entityId)
                    .description(description)
                    .oldValue(oldValue)
                    .newValue(newValue)
                    .ipAddress(ipAddress)
                    .build();

            activityLogRepository.save(activityLog);
            log.debug("Activity logged with values: {} - {} - {}", action, entityType, description);
        } catch (Exception e) {
            log.error("Failed to log activity with values: {}", e.getMessage(), e);
        }
    }

    /**
     * Get all activity logs with filters.
     */
    @Transactional(readOnly = true)
    public PageResponse<ActivityLogResponse> getAllLogs(ActivityLogFilterRequest filter) {
        Sort sort = filter.getSortDir().equalsIgnoreCase("asc")
                ? Sort.by(filter.getSortBy()).ascending()
                : Sort.by(filter.getSortBy()).descending();

        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);

        Specification<ActivityLog> spec = ActivityLogSpecification.withFilters(
                filter.getUserId(),
                filter.getAction(),
                filter.getEntityType(),
                filter.getStartDate(),
                filter.getEndDate()
        );

        Page<ActivityLog> logPage = activityLogRepository.findAll(spec, pageable);
        Page<ActivityLogResponse> responsePage = logPage.map(activityLogMapper::toResponse);
        return PageResponse.fromPage(responsePage);
    }

    /**
     * Get logs for a specific entity.
     */
    @Transactional(readOnly = true)
    public List<ActivityLogResponse> getLogsForEntity(String entityType, Long entityId) {
        List<ActivityLog> logs = activityLogRepository
                .findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId);
        return logs.stream()
                .map(activityLogMapper::toResponse)
                .toList();
    }

    /**
     * Get a log by ID.
     */
    @Transactional(readOnly = true)
    public ActivityLogResponse getLogById(Long id) {
        ActivityLog log = activityLogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Activity log not found with id: " + id));
        return activityLogMapper.toResponse(log);
    }

    /**
     * Delete a log.
     */
    @Transactional
    public void deleteLog(Long id) {
        activityLogRepository.deleteById(id);
    }

    /**
     * Delete old logs (cleanup).
     */
    @Transactional
    public void deleteOldLogs(int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        activityLogRepository.deleteByCreatedAtBefore(cutoffDate);
        log.info("Deleted activity logs older than {} days", daysToKeep);
    }

    /**
     * Get client IP address from current request.
     */
    private String getClientIp() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String ip = request.getHeader("X-Forwarded-For");
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getHeader("X-Real-IP");
                }
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getRemoteAddr();
                }
                // Handle multiple IPs in X-Forwarded-For
                if (ip != null && ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        } catch (Exception e) {
            log.warn("Failed to get client IP: {}", e.getMessage());
        }
        return null;
    }
}
