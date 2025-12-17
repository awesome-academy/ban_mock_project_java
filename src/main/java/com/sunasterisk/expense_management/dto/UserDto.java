package com.sunasterisk.expense_management.dto;

import com.sunasterisk.expense_management.entity.User;
import lombok.*;

/**
 * DTO for User management in admin panel
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private Long id;
    private String name;
    private String email;
    private String password; // Only used for create/update
    private String phone;
    private User.Role role;
    private Boolean active;

    /**
     * Create DTO from Entity
     */
    public static UserDto fromEntity(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .active(user.getActive())
                .build();
    }
}
