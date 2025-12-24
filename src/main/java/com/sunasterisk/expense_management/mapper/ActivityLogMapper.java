package com.sunasterisk.expense_management.mapper;

import com.sunasterisk.expense_management.dto.activitylog.ActivityLogResponse;
import com.sunasterisk.expense_management.entity.ActivityLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ActivityLogMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.name", target = "userName")
    @Mapping(source = "user.email", target = "userEmail")
    ActivityLogResponse toResponse(ActivityLog activityLog);
}
