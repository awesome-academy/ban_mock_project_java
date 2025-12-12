# Report & Analytics API Documentation

## Overview

Report & Analytics APIs cung cấp các báo cáo và phân tích tài chính chi tiết, bao gồm:
- Báo cáo theo thời gian (tháng, quý, năm)
- Phân bố chi tiêu theo danh mục
- So sánh thu nhập vs chi tiêu
- Phân tích xu hướng chi tiêu

---

## Endpoints

### 1. Report by Time Period

**Endpoint:** `POST /api/reports/by-time`

**Description:** Báo cáo tổng quan theo khoảng thời gian. Tự động nhận diện period type (month/quarter/year/custom).

**Headers:**
```
Authorization: Bearer <token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "startDate": "2025-12-01",
  "endDate": "2025-12-31",
  "categoryId": null
}
```

**Parameters:**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| startDate | LocalDate | Yes | Ngày bắt đầu (YYYY-MM-DD) |
| endDate | LocalDate | Yes | Ngày kết thúc (YYYY-MM-DD) |
| categoryId | Long | No | Lọc theo category (null = tất cả) |

**Response:** `200 OK`
```json
{
  "startDate": "2025-12-01",
  "endDate": "2025-12-31",
  "totalExpense": 5620000.00,
  "totalIncome": 0.00,
  "balance": -5620000.00,
  "expenseCount": 11,
  "incomeCount": 0,
  "averageExpense": 510909.09,
  "averageIncome": 0,
  "period": "month"
}
```

**Period Auto-Detection:**
| Days | Period Type |
|------|-------------|
| ≤ 31 | month |
| ≤ 92 | quarter |
| ≤ 366 | year |
| > 366 | custom |

**Example:**
```bash
curl -X POST "http://localhost:8080/api/reports/by-time" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "startDate": "2025-12-01",
    "endDate": "2025-12-31"
  }'
```

---

### 2. Category Distribution (Pie Chart Data)

**Endpoint:** `POST /api/reports/by-category`

**Description:** Phân bố chi tiêu theo danh mục. Dùng để vẽ pie chart hoặc bar chart.

**Headers:**
```
Authorization: Bearer <token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "startDate": "2025-12-01",
  "endDate": "2025-12-31"
}
```

**Response:** `200 OK`
```json
{
  "startDate": "2025-12-01",
  "endDate": "2025-12-31",
  "totalExpense": 5620000.00,
  "categories": [
    {
      "categoryId": 1,
      "categoryName": "Ăn uống",
      "categoryIcon": "🍔",
      "categoryColor": "#FF6B6B",
      "amount": 3500000.00,
      "count": 8,
      "percentage": 62.28
    },
    {
      "categoryId": 2,
      "categoryName": "Di chuyển",
      "categoryIcon": "🚗",
      "categoryColor": "#4ECDC4",
      "amount": 2120000.00,
      "count": 3,
      "percentage": 37.72
    }
  ]
}
```

**Features:**
- ✅ Grouped by category
- ✅ Sorted by amount (DESC)
- ✅ Auto-calculate percentage
- ✅ Includes category metadata (icon, color)

**Example:**
```bash
curl -X POST "http://localhost:8080/api/reports/by-category" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "startDate": "2025-01-01",
    "endDate": "2025-12-31"
  }'
```

---

### 3. Income vs Expense Comparison

**Endpoint:** `POST /api/reports/income-vs-expense`

**Description:** So sánh thu nhập và chi tiêu. Tính balance, savings rate, và đánh giá financial health.

**Headers:**
```
Authorization: Bearer <token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "startDate": "2025-12-01",
  "endDate": "2025-12-31"
}
```

**Response:** `200 OK`
```json
{
  "startDate": "2025-12-01",
  "endDate": "2025-12-31",
  "totalIncome": 15000000.00,
  "incomeCount": 2,
  "averageIncome": 7500000.00,
  "totalExpense": 5620000.00,
  "expenseCount": 11,
  "averageExpense": 510909.09,
  "balance": 9380000.00,
  "savingsRate": 62.53,
  "financialHealth": "SURPLUS"
}
```

**Financial Health Indicators:**
| Savings Rate | Health Status |
|--------------|---------------|
| ≥ 10% | SURPLUS (Dư thừa) |
| -10% to 10% | BALANCED (Cân bằng) |
| ≤ -10% | DEFICIT (Thâm hụt) |
| No income | UNKNOWN |

**Example:**
```bash
curl -X POST "http://localhost:8080/api/reports/income-vs-expense" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "startDate": "2025-01-01",
    "endDate": "2025-12-31"
  }'
```

---

### 4. Trend Analysis

**Endpoint:** `POST /api/reports/trend?period={MONTHLY|QUARTERLY|YEARLY}`

**Description:** Phân tích xu hướng chi tiêu theo thời gian. Tính change percentage giữa các period.

**Headers:**
```
Authorization: Bearer <token>
Content-Type: application/json
```

**Query Parameters:**
| Parameter | Type | Required | Default | Values |
|-----------|------|----------|---------|--------|
| period | String | No | MONTHLY | MONTHLY, QUARTERLY, YEARLY |

**Request Body:**
```json
{
  "startDate": "2025-01-01",
  "endDate": "2025-12-31"
}
```

**Response:** `200 OK`
```json
{
  "period": "MONTHLY",
  "trends": [
    {
      "period": "2025-01",
      "year": 2025,
      "month": 1,
      "quarter": null,
      "totalExpense": 4500000.00,
      "totalIncome": 10000000.00,
      "balance": 5500000.00,
      "expenseCount": 15,
      "incomeCount": 1,
      "changePercentage": null
    },
    {
      "period": "2025-02",
      "year": 2025,
      "month": 2,
      "quarter": null,
      "totalExpense": 5200000.00,
      "totalIncome": 10000000.00,
      "balance": 4800000.00,
      "expenseCount": 18,
      "incomeCount": 1,
      "changePercentage": 15.56
    }
  ],
  "averageExpense": 4850000.00,
  "maxExpense": 5200000.00,
  "minExpense": 4500000.00,
  "trendDirection": "INCREASING"
}
```

**Trend Direction:**
- **INCREASING**: Chi tiêu tăng > 10%
- **DECREASING**: Chi tiêu giảm > 10%
- **STABLE**: Biến động < 10%

**Period Formats:**
| Period Type | Format | Example |
|-------------|--------|---------|
| MONTHLY | YYYY-MM | 2025-01 |
| QUARTERLY | YYYY-QN | 2025-Q1 |
| YEARLY | YYYY | 2025 |

**Examples:**

Monthly trend:
```bash
curl -X POST "http://localhost:8080/api/reports/trend?period=MONTHLY" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "startDate": "2025-01-01",
    "endDate": "2025-12-31"
  }'
```

Quarterly trend:
```bash
curl -X POST "http://localhost:8080/api/reports/trend?period=QUARTERLY" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "startDate": "2024-01-01",
    "endDate": "2025-12-31"
  }'
```

Yearly trend:
```bash
curl -X POST "http://localhost:8080/api/reports/trend?period=YEARLY" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "startDate": "2020-01-01",
    "endDate": "2025-12-31"
  }'
```

---

## Use Cases

### Dashboard Overview
```bash
# 1. Tổng quan tháng hiện tại
POST /api/reports/by-time
{
  "startDate": "2025-12-01",
  "endDate": "2025-12-31"
}

# 2. Biểu đồ phân bố category (pie chart)
POST /api/reports/by-category
{
  "startDate": "2025-12-01",
  "endDate": "2025-12-31"
}

# 3. Thu nhập vs Chi tiêu
POST /api/reports/income-vs-expense
{
  "startDate": "2025-12-01",
  "endDate": "2025-12-31"
}
```

### Trend Charts (Line/Bar Chart)
```bash
# Xu hướng 6 tháng gần nhất
POST /api/reports/trend?period=MONTHLY
{
  "startDate": "2025-07-01",
  "endDate": "2025-12-31"
}

# Xu hướng 4 quý gần nhất
POST /api/reports/trend?period=QUARTERLY
{
  "startDate": "2025-01-01",
  "endDate": "2025-12-31"
}

# Xu hướng 5 năm
POST /api/reports/trend?period=YEARLY
{
  "startDate": "2021-01-01",
  "endDate": "2025-12-31"
}
```

### Specific Category Analysis
```bash
# Phân tích chi tiêu "Ăn uống" trong năm
POST /api/reports/by-time
{
  "startDate": "2025-01-01",
  "endDate": "2025-12-31",
  "categoryId": 1
}
```

---

## Business Logic

### Calculations

**Balance:**
```
balance = totalIncome - totalExpense
```

**Savings Rate:**
```
savingsRate = (balance / totalIncome) * 100
```

**Category Percentage:**
```
percentage = (categoryAmount / totalExpense) * 100
```

**Change Percentage:**
```
changePercentage = ((currentPeriod - previousPeriod) / previousPeriod) * 100
```

**Average:**
```
average = total / count
```

### Trend Direction Algorithm

1. Split data into 2 halves
2. Calculate average for each half
3. Compare:
   - If diff > 10% of first half → INCREASING
   - If diff < -10% of first half → DECREASING
   - Otherwise → STABLE

### Financial Health Logic

```java
if (totalIncome == 0) return "UNKNOWN";

BigDecimal ratio = balance / totalIncome * 100;

if (ratio >= 10) return "SURPLUS";
if (ratio <= -10) return "DEFICIT";
return "BALANCED";
```

---

## Data Requirements

### For Accurate Reports:
- ✅ Expenses with `expenseDate`
- ✅ Incomes with `incomeDate`
- ✅ Categories assigned to expenses
- ✅ Valid date ranges (endDate >= startDate)

### Query Optimization:
- Database indexes on `(user_id, expense_date)`
- Database indexes on `(user_id, category_id, expense_date)`
- Use date ranges wisely (avoid very large ranges)

---

## Error Responses

**400 Bad Request:**
```json
{
  "timestamp": "2025-12-10T10:00:00",
  "status": 400,
  "error": "Yêu cầu không hợp lệ",
  "message": "Loại báo cáo không hợp lệ. Chỉ chấp nhận MONTHLY, QUARTERLY, YEARLY"
}
```

**401 Unauthorized:**
```json
{
  "timestamp": "2025-12-10T10:00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid or expired token"
}
```

---

## Technical Implementation

### Repository Queries

**ExpenseRepository:**
```java
- sumByUserAndDateBetween()
- countByUserAndDateBetween()
- groupByCategoryAndDateBetween()
- groupByMonthAndDateBetween()
- groupByQuarterAndDateBetween()
- groupByYearAndDateBetween()
```

**IncomeRepository:**
```java
- sumByUserAndDateBetween()
- countByUserAndDateBetween()
- groupByMonthAndDateBetween()
- groupByQuarterAndDateBetween()
- groupByYearAndDateBetween()
```

### Performance
- ✅ All queries use aggregation (SUM, COUNT, GROUP BY)
- ✅ Indexed by user_id and date fields
- ✅ Read-only transactions
- ✅ BigDecimal for financial precision

---

## Frontend Integration Examples

### Chart.js - Pie Chart (Category Distribution)
```javascript
const response = await fetch('/api/reports/by-category', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    startDate: '2025-12-01',
    endDate: '2025-12-31'
  })
});

const data = await response.json();

const chartData = {
  labels: data.categories.map(c => c.categoryName),
  datasets: [{
    data: data.categories.map(c => c.amount),
    backgroundColor: data.categories.map(c => c.categoryColor)
  }]
};
```

### Chart.js - Line Chart (Trend)
```javascript
const response = await fetch('/api/reports/trend?period=MONTHLY', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    startDate: '2025-01-01',
    endDate: '2025-12-31'
  })
});

const data = await response.json();

const chartData = {
  labels: data.trends.map(t => t.period),
  datasets: [
    {
      label: 'Chi tiêu',
      data: data.trends.map(t => t.totalExpense),
      borderColor: '#FF6B6B'
    },
    {
      label: 'Thu nhập',
      data: data.trends.map(t => t.totalIncome),
      borderColor: '#4ECDC4'
    }
  ]
};
```

---

## Related Documentation
- [Expense API](README.md)
- [Income API](INCOME_API.md)
- [Budget API](BUDGET_API.md)
- [Category API](CATEGORY_API.md)
