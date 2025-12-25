package com.sunasterisk.expense_management.controller.admin;

import com.sunasterisk.expense_management.dto.PageResponse;
import com.sunasterisk.expense_management.dto.activitylog.ActivityLogFilterRequest;
import com.sunasterisk.expense_management.dto.activitylog.ActivityLogResponse;
import com.sunasterisk.expense_management.entity.ActivityLog.ActionType;
import com.sunasterisk.expense_management.service.ActivityLogService;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminActivityLogController extends BaseAdminController {

    private static final String MODULE = "activity-logs";

    private final ActivityLogService activityLogService;

    public AdminActivityLogController(ActivityLogService activityLogService, MessageSource messageSource) {
        super(messageSource);
        this.activityLogService = activityLogService;
    }

    @GetMapping("/activity-logs")
    public String index(Model model,
                        @RequestParam(required = false) Long userId,
                        @RequestParam(required = false) ActionType action,
                        @RequestParam(required = false) String entityType,
                        @RequestParam(required = false) String startDate,
                        @RequestParam(required = false) String endDate,
                        @RequestParam(defaultValue = "0") Integer page,
                        @RequestParam(defaultValue = "20") Integer size) {

        ActivityLogFilterRequest.ActivityLogFilterRequestBuilder filterBuilder = ActivityLogFilterRequest.builder()
                .userId(userId)
                .action(action)
                .entityType(entityType)
                .page(page)
                .size(size);

        if (parseLocalDate(startDate) != null) {
            filterBuilder.startDate(parseLocalDate(startDate));
        }
        if (parseLocalDate(endDate) != null) {
            filterBuilder.endDate(parseLocalDate(endDate));
        }

        ActivityLogFilterRequest filter = filterBuilder.build();
        PageResponse<ActivityLogResponse> response = activityLogService.getAllLogs(filter);

        model.addAttribute("activeMenu", MODULE);
        model.addAttribute("logs", response.getContent());
        model.addAttribute("currentPage", response.getPageNumber());
        model.addAttribute("totalPages", response.getTotalPages());
        model.addAttribute("totalElements", response.getTotalElements());
        model.addAttribute("filter", filter);
        model.addAttribute("actionTypes", ActionType.values());

        return viewIndex(MODULE);
    }

    @GetMapping("/activity-logs/{id}")
    public String detail(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            ActivityLogResponse log = activityLogService.getLogById(id);
            model.addAttribute("activeMenu", MODULE);
            model.addAttribute("log", log);
            return viewDetail(MODULE);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return redirectToIndex(MODULE);
        }
    }

    @DeleteMapping("/activity-logs/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            activityLogService.deleteLog(id);
            redirectAttributes.addFlashAttribute("success",
                    getMessage("admin.activity.log.deleted.success"));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return redirectToIndex(MODULE);
    }
}
