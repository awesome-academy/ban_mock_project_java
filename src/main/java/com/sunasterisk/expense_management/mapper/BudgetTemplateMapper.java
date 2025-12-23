package com.sunasterisk.expense_management.mapper;

import com.sunasterisk.expense_management.dto.budgettemplate.BudgetTemplateItemDto;
import com.sunasterisk.expense_management.dto.budgettemplate.BudgetTemplateRequest;
import com.sunasterisk.expense_management.dto.budgettemplate.BudgetTemplateResponse;
import com.sunasterisk.expense_management.entity.BudgetTemplate;
import com.sunasterisk.expense_management.entity.BudgetTemplateItem;
import com.sunasterisk.expense_management.entity.Category;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface BudgetTemplateMapper {

    @Mapping(target = "items", source = "items")
    BudgetTemplateResponse toResponse(BudgetTemplate budgetTemplate);

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    BudgetTemplateItemDto toItemDto(BudgetTemplateItem item);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    BudgetTemplate toEntity(BudgetTemplateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(BudgetTemplateRequest request, @MappingTarget BudgetTemplate budgetTemplate);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "budgetTemplate", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    BudgetTemplateItem toItemEntity(BudgetTemplateItemDto dto);

    /**
     * Map categoryId to Category entity
     */
    default Category mapCategory(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        Category category = new Category();
        category.setId(categoryId);
        return category;
    }
}
