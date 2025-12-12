package com.sun.expense_management.dto.category;

import com.sun.expense_management.entity.Category;
import com.sun.expense_management.entity.Category.CategoryType;
import com.sun.expense_management.validation.ValidSortDirection;
import com.sun.expense_management.validation.ValidSortFields;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryFilterRequest {

    private String name;
    private CategoryType type;
    private Boolean active;

    @Builder.Default
    private Integer page = 0;

    @Builder.Default
    private Integer size = 10;

    @Builder.Default
    @ValidSortFields(
        entityClass = Category.class,
        allowedFields = {"id", "name", "type", "icon", "color", "active", "isDefault", "createdAt", "updatedAt"},
        message = "{validation.category.sort.fields.invalid}"
    )
    private String sortBy = "name";

    @Builder.Default
    @ValidSortDirection
    private String sortDir = "asc";
}
