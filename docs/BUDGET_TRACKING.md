# Budget Tracking - Automatic `spentAmount` Updates

## Overview

Hệ thống tự động cập nhật `spentAmount` của Budget mỗi khi có thay đổi Expense (create/update/delete). Điều này đảm bảo dữ liệu ngân sách luôn chính xác real-time.

## Implementation

### 1. Database Query

**ExpenseRepository.sumByUserAndCategoryAndYearMonth()**

```java
@Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e " +
       "WHERE e.user.id = :userId " +
       "AND (:categoryId IS NULL OR e.category.id = :categoryId) " +
       "AND YEAR(e.expenseDate) = :year " +
       "AND MONTH(e.expenseDate) = :month")
BigDecimal sumByUserAndCategoryAndYearMonth(
    @Param("userId") Long userId,
    @Param("categoryId") Long categoryId,
    @Param("year") Integer year,
    @Param("month") Integer month
);
```

- Tính tổng expenses theo user, category, year, month
- COALESCE đảm bảo trả về 0 thay vì null
- Hỗ trợ categoryId = NULL (budget tổng)

### 2. Auto-Update Logic

**ExpenseService.updateBudgetSpentAmount()**

```java
private void updateBudgetSpentAmount(Long userId, Long categoryId, LocalDate date) {
    YearMonth yearMonth = YearMonth.from(date);

    budgetRepository.findByUser_IdAndCategory_IdAndYearAndMonth(
            userId, categoryId, yearMonth.getYear(), yearMonth.getMonthValue()
    ).ifPresent(budget -> {
        // Recalculate total spent
        BigDecimal totalSpent = expenseRepository.sumByUserAndCategoryAndYearMonth(
                userId, categoryId, yearMonth.getYear(), yearMonth.getMonthValue()
        );

        budget.setSpentAmount(totalSpent != null ? totalSpent : BigDecimal.ZERO);
        budgetRepository.save(budget);
    });
}
```

### 3. Trigger Points

#### A. Create Expense
```java
@Transactional
public ExpenseResponse createExpense(ExpenseRequest request) {
    // ... create expense logic ...
    expense = expenseRepository.save(expense);

    // ✅ Auto-update budget
    updateBudgetSpentAmount(user.getId(), category.getId(), expense.getExpenseDate());

    return expenseMapper.toResponse(expense);
}
```

#### B. Update Expense
```java
@Transactional
public ExpenseResponse updateExpense(Long id, ExpenseRequest request) {
    // Track old values
    Long oldCategoryId = expense.getCategory().getId();
    LocalDate oldDate = expense.getExpenseDate();

    // ... update expense logic ...
    expense = expenseRepository.save(expense);

    // ✅ Update old budget
    updateBudgetSpentAmount(user.getId(), oldCategoryId, oldDate);

    // ✅ Update new budget if category or month changed
    if (!oldCategoryId.equals(category.getId()) ||
        !YearMonth.from(oldDate).equals(YearMonth.from(expense.getExpenseDate()))) {
        updateBudgetSpentAmount(user.getId(), category.getId(), expense.getExpenseDate());
    }

    return expenseMapper.toResponse(expense);
}
```

**Why update both old and new?**
- Nếu user đổi category: `Ăn uống → Di chuyển`
  - Budget "Ăn uống" giảm spentAmount
  - Budget "Di chuyển" tăng spentAmount
- Nếu user đổi tháng: `2025-11 → 2025-12`
  - Budget tháng 11 giảm
  - Budget tháng 12 tăng

#### C. Delete Expense
```java
@Transactional
public void deleteExpense(Long id) {
    Expense expense = expenseRepository.findByIdAndUser(id, user)
            .orElseThrow(() -> new ResourceNotFoundException(...));

    Long categoryId = expense.getCategory().getId();
    LocalDate expenseDate = expense.getExpenseDate();

    expenseRepository.delete(expense);

    // ✅ Update budget after deletion
    updateBudgetSpentAmount(user.getId(), categoryId, expenseDate);
}
```

## Test Results

### Test Case 1: Create Expense
```bash
# Before: spentAmount = 0
POST /api/expenses {"amount": 150000, "categoryId": 1, "date": "2025-12-10"}
# After: spentAmount = 150000 ✅
```

### Test Case 2: Create Another Expense
```bash
# Before: spentAmount = 570000
POST /api/expenses {"amount": 50000, "categoryId": 1, "date": "2025-12-10"}
# After: spentAmount = 620000 ✅
```

### Test Case 3: Delete Expense
```bash
# Before: spentAmount = 620000
DELETE /api/expenses/14
# After: spentAmount = 570000 ✅
```

### Test Case 4: Update Expense Amount
```bash
# Before: spentAmount = 570000 (150k expense)
PUT /api/expenses/13 {"amount": 200000}
# After: spentAmount = 620000 (200k expense) ✅
```

### Test Case 5: Change Category
```bash
# Before:
#   Budget "Ăn uống" = 620000
#   Budget "Di chuyển" = 0

PUT /api/expenses/13 {"categoryId": 2, "amount": 200000}

# After:
#   Budget "Ăn uống" = 420000 (giảm 200k)
#   Budget "Di chuyển" = 200000 (tăng 200k) ✅
```

## Calculated Fields

Budget entity có các helper methods dựa trên `spentAmount`:

```java
// Remaining amount
public BigDecimal getRemainingAmount() {
    return amountLimit.subtract(spentAmount);
}

// Usage percentage
public double getUsagePercentage() {
    if (amountLimit.compareTo(BigDecimal.ZERO) == 0) return 0;
    return spentAmount.divide(amountLimit, 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100))
            .doubleValue();
}

// Over budget check
public boolean isOverBudget() {
    return spentAmount.compareTo(amountLimit) > 0;
}

// Alert trigger
public boolean shouldAlert() {
    return getUsagePercentage() >= alertThreshold && !isAlertSent;
}
```

## Performance Considerations

### Pros ✅
- **Real-time accuracy**: Luôn đúng, không cần sync
- **Simple queries**: Chỉ 1 SUM query, index trên (user_id, category_id, year, month)
- **Alert-ready**: Có thể trigger alert ngay khi vượt threshold
- **Audit trail**: updatedAt timestamp tự động cập nhật

### Optimizations
1. **Database indexes** đã có:
   ```sql
   CREATE INDEX idx_expense_tracking ON expenses(user_id, category_id, expense_date);
   CREATE INDEX idx_budget_period ON budgets(user_id, category_id, year, month);
   ```

2. **Transaction boundaries**:
   - Expense save + Budget update trong cùng @Transactional
   - Rollback tự động nếu có lỗi

3. **Optional.ifPresent()**:
   - Chỉ update nếu budget tồn tại
   - Không throw exception nếu user chưa tạo budget

## Future Enhancements

### 1. Batch Updates
Nếu import nhiều expenses cùng lúc:
```java
public void importExpenses(List<ExpenseRequest> expenses) {
    // Save all expenses first
    List<Expense> savedExpenses = expenseRepository.saveAll(...);

    // Group by (categoryId, yearMonth)
    Map<String, List<Expense>> grouped = savedExpenses.stream()
        .collect(Collectors.groupingBy(e ->
            e.getCategory().getId() + "-" + YearMonth.from(e.getExpenseDate())
        ));

    // Update each budget once
    grouped.forEach((key, expenses) -> {
        updateBudgetSpentAmount(userId, categoryId, date);
    });
}
```

### 2. Alert Notifications
Khi `shouldAlert() == true`:
```java
if (budget.shouldAlert()) {
    notificationService.sendBudgetAlert(budget);
    budget.setIsAlertSent(true);
}
```

### 3. Budget without Category
Hỗ trợ "Tổng ngân sách" (không gắn category):
```java
// In sumByUserAndCategoryAndYearMonth query
// (:categoryId IS NULL OR e.category.id = :categoryId)
// Already supported! ✅
```

## Dependencies

- `ExpenseRepository.sumByUserAndCategoryAndYearMonth()` - Tính tổng chi tiêu
- `BudgetRepository.findByUser_IdAndCategory_IdAndYearAndMonth()` - Tìm budget
- `@Transactional` - Đảm bảo data consistency
- `YearMonth` - Java 8 Time API

## Related Files

```
repository/
├── ExpenseRepository.java       (sumByUserAndCategoryAndYearMonth query)
└── BudgetRepository.java        (findByUser_IdAndCategory_IdAndYearAndMonth)

service/
└── ExpenseService.java          (updateBudgetSpentAmount helper method)

entity/
└── Budget.java                  (calculated fields: getUsagePercentage, isOverBudget, shouldAlert)
```

## Migration Notes

### Before (Manual tracking)
```java
// User phải manually update budget
PUT /api/budgets/1 {"spentAmount": 1000000}
```

### After (Automatic tracking) ✅
```java
// System tự động update khi có expense
POST /api/expenses {"amount": 100000, ...}  // Budget auto-updates!
PUT /api/expenses/1 {"amount": 150000, ...} // Budget recalculates!
DELETE /api/expenses/1                       // Budget adjusts!
```

### Backfill existing budgets
Nếu đã có expenses nhưng budget.spentAmount = 0:
```java
@Transactional
public void backfillBudgetSpentAmounts() {
    List<Budget> budgets = budgetRepository.findAllByActiveTrue();

    budgets.forEach(budget -> {
        BigDecimal totalSpent = expenseRepository.sumByUserAndCategoryAndYearMonth(
            budget.getUser().getId(),
            budget.getCategory().getId(),
            budget.getYear(),
            budget.getMonth()
        );

        budget.setSpentAmount(totalSpent != null ? totalSpent : BigDecimal.ZERO);
    });

    budgetRepository.saveAll(budgets);
}
```
