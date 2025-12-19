package com.sunasterisk.expense_management.dto.budgettemplate;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetTemplateRequest {

    @NotBlank(message = "{template.name.required}")
    @Size(max = 200, message = "{template.name.max.length}")
    private String name;

    @Size(max = 500, message = "{validation.max.length}")
    private String description;

    @Builder.Default
    private Boolean active = true;

    @Valid
    @Builder.Default
    private List<BudgetTemplateItemDto> items = new ArrayList<>();
}
