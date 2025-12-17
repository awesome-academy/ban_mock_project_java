package com.sunasterisk.expense_management.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity đại diện cho nguồn thu nhập
 */
@Entity
@Table(name = "incomes", indexes = {
    @Index(name = "idx_income_user", columnList = "user_id"),
    @Index(name = "idx_income_category", columnList = "category_id"),
    @Index(name = "idx_income_date", columnList = "income_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Income {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "{income.name.required}")
    @Size(max = 200, message = "{income.name.max.length}")
    @Column(nullable = false, length = 200)
    private String name;

    @NotNull(message = "{income.amount.required}")
    @Positive(message = "{income.amount.positive}")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @NotNull(message = "{income.date.required}")
    @Column(name = "income_date", nullable = false)
    private LocalDate incomeDate;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(length = 100)
    private String source;

    @Column(name = "is_recurring", nullable = false)
    @Builder.Default
    private Boolean isRecurring = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "recurring_type", length = 20)
    private RecurringType recurringType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum RecurringType {
        DAILY, WEEKLY, MONTHLY, YEARLY
    }
}
