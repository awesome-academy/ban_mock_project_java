package com.sunasterisk.expense_management.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity đại diện cho danh mục chi tiêu/thu nhập
 */
@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "{category.name.required}")
    @Size(max = 100, message = "{category.name.max.length}")
    @Column(nullable = false, length = 100)
    private String name;

    @Size(max = 255, message = "{validation.max.length}")
    @Column(length = 255)
    private String description;

    @Column(length = 50)
    private String icon;

    @Column(length = 20)
    private String color;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private CategoryType type = CategoryType.EXPENSE;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private Boolean isDefault = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Expense> expenses = new ArrayList<>();

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Income> incomes = new ArrayList<>();

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Budget> budgets = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        validateCategoryIntegrity();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        validateCategoryIntegrity();
    }

    /**
     * Validate category data integrity
     * Rules:
     * - Default categories (isDefault = true) must have user = null
     * - Custom categories (isDefault = false) must have user != null
     *
     * Note: These are hardcoded English messages for database-level validation.
     * Service-level validation uses i18n messages from:
     * - category.integrity.default.must.not.have.user
     * - category.integrity.custom.must.have.user
     */
    private void validateCategoryIntegrity() {
        if (isDefault != null && isDefault) {
            // Default categories should not belong to any specific user
            if (user != null) {
                throw new IllegalStateException(
                        "Data integrity violation: Default category cannot have a user owner");
            }
        } else {
            // Non-default (custom) categories must have an owner
            if (user == null) {
                throw new IllegalStateException(
                        "Data integrity violation: Non-default category must have a user owner");
            }
        }
    }

    public enum CategoryType {
        EXPENSE, INCOME
    }
}
