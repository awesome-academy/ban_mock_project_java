package com.sun.expense_management.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity đại diện cho khoản chi tiêu
 */
@Entity
@Table(name = "expenses", indexes = {
    @Index(name = "idx_expense_user", columnList = "user_id"),
    @Index(name = "idx_expense_category", columnList = "category_id"),
    @Index(name = "idx_expense_date", columnList = "expense_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "{expense.name.required}")
    @Size(max = 200, message = "{expense.name.max.length}")
    @Column(nullable = false, length = 200)
    private String name;

    @NotNull(message = "{expense.amount.required}")
    @Positive(message = "{expense.amount.positive}")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @NotNull(message = "{expense.date.required}")
    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(length = 100)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 30)
    @Builder.Default
    private PaymentMethod paymentMethod = PaymentMethod.CASH;

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

    @OneToMany(mappedBy = "expense", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Attachment> attachments = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum PaymentMethod {
        CASH, CREDIT_CARD, DEBIT_CARD, BANK_TRANSFER, E_WALLET, OTHER
    }

    public enum RecurringType {
        DAILY, WEEKLY, MONTHLY, YEARLY
    }
}
