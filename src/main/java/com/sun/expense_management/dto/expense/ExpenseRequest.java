package com.sun.expense_management.dto.expense;

import com.sun.expense_management.entity.Expense.PaymentMethod;
import com.sun.expense_management.entity.Expense.RecurringType;
import com.sun.expense_management.validation.ValidRecurringExpense;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@ValidRecurringExpense
public class ExpenseRequest {

    @NotBlank(message = "Tên chi tiêu không được để trống")
    @Size(max = 200, message = "Tên chi tiêu không được vượt quá 200 ký tự")
    private String name;

    @NotNull(message = "Số tiền không được để trống")
    @Positive(message = "Số tiền phải lớn hơn 0")
    @DecimalMax(value = "9999999999999.99", message = "Số tiền vượt quá giới hạn cho phép")
    private BigDecimal amount;

    @NotNull(message = "Ngày chi tiêu không được để trống")
    @PastOrPresent(message = "Ngày chi tiêu không được là ngày trong tương lai")
    private LocalDate expenseDate;

    @NotNull(message = "Danh mục không được để trống")
    private Long categoryId;

    @Size(max = 1000, message = "Ghi chú không được vượt quá 1000 ký tự")
    private String note;

    @Size(max = 100, message = "Địa điểm không được vượt quá 100 ký tự")
    private String location;

    @NotNull(message = "Phương thức thanh toán không được để trống")
    private PaymentMethod paymentMethod = PaymentMethod.CASH;

    @NotNull(message = "Trường chi tiêu định kỳ không được để trống")
    private Boolean isRecurring = false;

    private RecurringType recurringType;
}
