package com.sun.expense_management.dto.category;

import com.sun.expense_management.entity.Category.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequest {

    @NotBlank(message = "{category.name.required}")
    @Size(max = 100, message = "{category.name.max.length}")
    private String name;

    @Size(max = 255, message = "{validation.max.length}")
    private String description;

    @Size(max = 50, message = "{validation.max.length}")
    private String icon;

    @Size(max = 20, message = "{validation.max.length}")
    private String color;

    @NotNull(message = "{validation.required}")
    private CategoryType type;

    @Builder.Default
    private Boolean active = true;
}
