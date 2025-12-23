package com.sunasterisk.expense_management.dto;

import com.sunasterisk.expense_management.entity.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDto {
    private Long id;

    @NotBlank(message = "{admin.category.name.required}")
    @Size(max = 100, message = "{admin.category.name.max.length}")
    private String name;

    @Size(max = 255, message = "{validation.max.length}")
    private String description;

    private Category.CategoryType type;
    private String icon;
    private String color;
    private Boolean active;
    private Boolean isDefault;

    public static CategoryDto fromEntity(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .type(category.getType())
                .icon(category.getIcon())
                .color(category.getColor())
                .active(category.getActive())
                .isDefault(category.getIsDefault())
                .build();
    }
}
