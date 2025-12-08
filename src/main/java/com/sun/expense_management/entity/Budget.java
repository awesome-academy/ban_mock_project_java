package com.sun.expense_management.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;

/**
 * Entity đại diện cho ngân sách
 */
@Entity
@Table(name = "budgets", indexes = {
    @Index(name = "idx_budget_user", columnList = "user_id"),
    @Index(name = "idx_budget_category", columnList = "category_id"),
    @Index(name = "idx_budget_period", columnList = "year, month")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "{budget.name.required}")
    @Size(max = 200, message = "{budget.name.max.length}")
    @Column(nullable = false, length = 200)
    private String name;

    @NotNull(message = "{budget.amount.required}")
    @Positive(message = "{budget.amount.positive}")
    @Column(name = "amount_limit", nullable = false, precision = 15, scale = 2)
    private BigDecimal amountLimit;

    @Column(name = "spent_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal spentAmount = BigDecimal.ZERO;

    @NotNull(message = "{budget.year.required}")
    @Column(nullable = false)
    private Integer year;

    @NotNull(message = "{budget.month.required}")
    @Column(nullable = false)
    private Integer month;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "alert_threshold")
    @Builder.Default
    private Integer alertThreshold = 80; // Cảnh báo khi đạt 80% ngân sách

    @Column(name = "is_alert_sent", nullable = false)
    @Builder.Default
    private Boolean isAlertSent = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
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

    /**
     * Tính phần trăm đã sử dụng
     */
    public double getUsagePercentage() {
        if (amountLimit == null || amountLimit.compareTo(BigDecimal.ZERO) == 0) {
            return 0;
        }
        return spentAmount.divide(amountLimit, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }

    /**
     * Kiểm tra xem có vượt ngân sách không
     */
    public boolean isOverBudget() {
        return spentAmount.compareTo(amountLimit) > 0;
    }

    /**
     * Kiểm tra xem có cần cảnh báo không
     */
    public boolean shouldAlert() {
        return getUsagePercentage() >= alertThreshold && !isAlertSent;
    }

    /**
     * Lấy số tiền còn lại
     */
    public BigDecimal getRemainingAmount() {
        return amountLimit.subtract(spentAmount);
    }

    /**
     * Lấy YearMonth
     */
    public YearMonth getYearMonth() {
        return YearMonth.of(year, month);
    }
}
