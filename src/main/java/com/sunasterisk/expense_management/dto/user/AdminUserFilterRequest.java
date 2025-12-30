package com.sunasterisk.expense_management.dto.user;

import com.sunasterisk.expense_management.entity.User;
import com.sunasterisk.expense_management.entity.User.Role;
import com.sunasterisk.expense_management.validation.ValidSortDirection;
import com.sunasterisk.expense_management.validation.ValidSortFields;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserFilterRequest {

    private String name;
    private String email;
    private Role role;
    private Boolean active;

    @Builder.Default
    private Integer page = 0;

    @Builder.Default
    private Integer size = 20;

    @Builder.Default
    @ValidSortFields(
        entityClass = User.class,
        allowedFields = {"id", "name", "email", "role", "active", "createdAt", "updatedAt"},
        message = "{validation.user.sort.fields.invalid}"
    )
    private String sortBy = "createdAt";

    @Builder.Default
    @ValidSortDirection
    private String sortDir = "desc";
}
