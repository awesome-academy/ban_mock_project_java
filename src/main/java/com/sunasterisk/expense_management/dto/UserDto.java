package com.sunasterisk.expense_management.dto;

import com.sunasterisk.expense_management.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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

    @NotBlank(message = "{user.name.required}")
    @Size(max = 100, message = "{user.name.max.length}")
    private String name;

    @NotBlank(message = "{user.email.required}")
    @Email(message = "{user.email.invalid}")
    private String email;

    @Size(min = 6, message = "{user.password.min.length}")
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
