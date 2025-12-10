package com.sun.expense_management.dto.category;

import com.sun.expense_management.entity.Category.CategoryType;
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
    private String sortBy = "name";

    @Builder.Default
    private String sortDir = "asc";
}
