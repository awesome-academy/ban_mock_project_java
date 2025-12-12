# Budget API Documentation

## Endpoints

### 1. Get Budgets (List with Filters)

**Endpoint:** `GET /api/budgets`

**Description:** Lấy danh sách ngân sách với pagination và filter. User chỉ thấy ngân sách của mình.

**Headers:**
```
Authorization: Bearer <token>
```

**Query Parameters:**
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| name | String | No | - | Filter theo tên ngân sách (partial match) |
| categoryId | Long | No | - | Filter theo category ID |
| year | Integer | No | - | Filter theo năm |
| month | Integer | No | - | Filter theo tháng (1-12) |
| isOverBudget | Boolean | No | - | Filter ngân sách vượt mức |
| active | Boolean | No | - | Filter theo trạng thái active |
| page | Integer | No | 0 | Số trang (bắt đầu từ 0) |
| size | Integer | No | 10 | Số lượng items mỗi trang |
| sortBy | String | No | year,month | Fields để sort (có thể multiple) |
| sortDir | String | No | desc | Hướng sort: `asc` hoặc `desc` |

**Response:** `200 OK`
```json
{
  "content": [
    {
      "id": 1,
      "name": "Ngân sách ăn uống tháng 12",
      "amountLimit": 5000000.00,
      "spentAmount": 0.00,
      "remainingAmount": 5000000.00,
      "usagePercentage": 0.0,
      "isOverBudget": false,
      "shouldAlert": false,
      "year": 2025,
      "month": 12,
      "note": "Ngân sách cho ăn uống gia đình",
      "alertThreshold": 80,
      "isAlertSent": false,
      "active": true,
      "createdAt": "2025-12-10T09:30:00",
      "updatedAt": "2025-12-10T09:30:00",
      "categoryId": 1,
      "categoryName": "Ăn uống",
      "categoryIcon": "🍔",
      "categoryColor": "#FF6B6B",
      "userId": 1,
      "userName": "Demo User"
    }
  ],
  "pageNumber": 0,
  "pageSize": 10,
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

**Examples:**
```bash
# Get all budgets
curl -X GET "http://localhost:8080/api/budgets" \
  -H "Authorization: Bearer <token>"

# Filter by year and month
curl -X GET "http://localhost:8080/api/budgets?year=2025&month=12" \
  -H "Authorization: Bearer <token>"

# Filter over budget
curl -X GET "http://localhost:8080/api/budgets?isOverBudget=true" \
  -H "Authorization: Bearer eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwiaWF0IjoxNzY1NDIxMDgxLCJleHAiOjE3NjU1MDc0ODF9.4e08pO1tNoOj8mHxCDAUZVf7VZ1Vl1eP3s48_XJ4tzGNYB0_yX1FRV6w8OdWInIb"

# With pagination and sorting
curl -X GET "http://localhost:8080/api/budgets?page=0&size=5&sortBy=createdAt&sortDir=desc" \
  -H "Authorization: Bearer <token>"
```

---

### 2. Get Budget by ID

**Endpoint:** `GET /api/budgets/{id}`

**Description:** Lấy chi tiết một ngân sách. User chỉ có thể xem ngân sách của mình.

**Headers:**
```
Authorization: Bearer <token>
```

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| id | Long | Yes | Budget ID |

**Response:** `200 OK`
```json
{
  "id": 1,
  "name": "Ngân sách ăn uống tháng 12",
  "amountLimit": 5000000.00,
  "spentAmount": 2500000.00,
  "remainingAmount": 2500000.00,
  "usagePercentage": 50.0,
  "isOverBudget": false,
  "shouldAlert": false,
  "year": 2025,
  "month": 12,
  "note": "Ngân sách cho ăn uống gia đình",
  "alertThreshold": 80,
  "isAlertSent": false,
  "active": true,
  "createdAt": "2025-12-10T09:30:00",
  "updatedAt": "2025-12-10T09:35:00",
  "categoryId": 1,
  "categoryName": "Ăn uống",
  "categoryIcon": "🍔",
  "categoryColor": "#FF6B6B",
  "userId": 1,
  "userName": "Demo User"
}
```

**Error Responses:**
- `404 Not Found` - Budget không tồn tại hoặc không có quyền truy cập

**Example:**
```bash
curl -X GET "http://localhost:8080/api/budgets/1" \
  -H "Authorization: Bearer <token>"
```

---

### 3. Create Budget

**Endpoint:** `POST /api/budgets`

**Description:** Tạo ngân sách mới. User không thể tạo 2 ngân sách cho cùng category trong cùng tháng/năm.

**Headers:**
```
Authorization: Bearer <token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "Ngân sách ăn uống tháng 12",
  "amountLimit": 5000000,
  "year": 2025,
  "month": 12,
  "categoryId": 1,
  "note": "Ngân sách cho ăn uống gia đình",
  "alertThreshold": 80,
  "active": true
}
```

**Field Validations:**
| Field | Type | Required | Validation |
|-------|------|----------|------------|
| name | String | Yes | Max 200 characters |
| amountLimit | BigDecimal | Yes | Min 0.01 |
| year | Integer | Yes | 2000-2100 |
| month | Integer | Yes | 1-12 |
| categoryId | Long | No | Must be valid category ID |
| note | String | No | Max 1000 characters |
| alertThreshold | Integer | No | 0-100, default: 80 |
| active | Boolean | No | Default: true |

**Response:** `201 Created`
```json
{
  "id": 1,
  "name": "Ngân sách ăn uống tháng 12",
  "amountLimit": 5000000.00,
  "spentAmount": 0.00,
  "remainingAmount": 5000000.00,
  "usagePercentage": 0.0,
  "isOverBudget": false,
  "shouldAlert": false,
  "year": 2025,
  "month": 12,
  "note": "Ngân sách cho ăn uống gia đình",
  "alertThreshold": 80,
  "isAlertSent": false,
  "active": true,
  "createdAt": "2025-12-10T09:30:00",
  "updatedAt": "2025-12-10T09:30:00",
  "categoryId": 1,
  "categoryName": "Ăn uống",
  "categoryIcon": "🍔",
  "categoryColor": "#FF6B6B",
  "userId": 1,
  "userName": "Demo User"
}
```

**Error Responses:**
- `400 Bad Request` - Validation errors hoặc budget already exists
- `404 Not Found` - Category không tồn tại

**Example:**
```bash
curl -X POST "http://localhost:8080/api/budgets" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Ngân sách ăn uống tháng 12",
    "amountLimit": 5000000,
    "year": 2025,
    "month": 12,
    "categoryId": 1,
    "note": "Ngân sách cho ăn uống gia đình",
    "alertThreshold": 80,
    "active": true
  }'
```

---

### 4. Update Budget

**Endpoint:** `PUT /api/budgets/{id}`

**Description:** Cập nhật ngân sách. User chỉ có thể update ngân sách của mình.

**Headers:**
```
Authorization: Bearer <token>
Content-Type: application/json
```

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| id | Long | Yes | Budget ID |

**Request Body:**
```json
{
  "name": "Ngân sách ăn uống tháng 12 - Updated",
  "amountLimit": 6000000,
  "year": 2025,
  "month": 12,
  "categoryId": 1,
  "note": "Tăng ngân sách do lạm phát",
  "alertThreshold": 85,
  "active": true
}
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "name": "Ngân sách ăn uống tháng 12 - Updated",
  "amountLimit": 6000000.00,
  "spentAmount": 2500000.00,
  "remainingAmount": 3500000.00,
  "usagePercentage": 41.67,
  "isOverBudget": false,
  "shouldAlert": false,
  "year": 2025,
  "month": 12,
  "note": "Tăng ngân sách do lạm phát",
  "alertThreshold": 85,
  "isAlertSent": false,
  "active": true,
  "createdAt": "2025-12-10T09:30:00",
  "updatedAt": "2025-12-10T09:40:00",
  "categoryId": 1,
  "categoryName": "Ăn uống",
  "categoryIcon": "🍔",
  "categoryColor": "#FF6B6B",
  "userId": 1,
  "userName": "Demo User"
}
```

**Error Responses:**
- `404 Not Found` - Budget không tồn tại hoặc không có quyền update
- `400 Bad Request` - Validation errors

**Example:**
```bash
curl -X PUT "http://localhost:8080/api/budgets/1" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Ngân sách ăn uống tháng 12 - Updated",
    "amountLimit": 6000000,
    "year": 2025,
    "month": 12,
    "categoryId": 1,
    "note": "Tăng ngân sách do lạm phát",
    "alertThreshold": 85,
    "active": true
  }'
```

---

### 5. Delete Budget (Soft Delete)

**Endpoint:** `DELETE /api/budgets/{id}`

**Description:** Xóa (soft delete) ngân sách bằng cách set `active = false`. User chỉ có thể xóa ngân sách của mình.

**Headers:**
```
Authorization: Bearer <token>
```

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| id | Long | Yes | Budget ID |

**Response:** `204 No Content`

**Error Responses:**
- `404 Not Found` - Budget không tồn tại hoặc không có quyền xóa

**Example:**
```bash
curl -X DELETE "http://localhost:8080/api/budgets/1" \
  -H "Authorization: Bearer <token>"
```

---

## Business Logic Notes

### 1. Budget Tracking
- **spentAmount**: Tự động tính từ Expense trong cùng category/year/month
- **remainingAmount**: amountLimit - spentAmount
- **usagePercentage**: (spentAmount / amountLimit) * 100

### 2. Alert System
- **alertThreshold**: Ngưỡng cảnh báo (% ngân sách đã dùng)
- **shouldAlert**: true khi usagePercentage >= alertThreshold và chưa gửi alert
- **isAlertSent**: Flag đánh dấu đã gửi alert

### 3. Budget Constraints
- **Unique Constraint**: Một user chỉ có 1 budget cho mỗi category trong mỗi tháng/năm
- **Category Optional**: Có thể tạo budget không gắn category (tổng ngân sách)

### 4. Permission Rules
- **Read**: User chỉ xem ngân sách của mình
- **Create**: User tự tạo ngân sách, tự động gán user_id
- **Update**: User chỉ update ngân sách của mình
- **Delete**: Soft delete (set active=false)

### 5. Dynamic Query with Specification Pattern
- Sử dụng JPA Specification cho dynamic queries
- Type-safe, maintainable code
- Hỗ trợ complex filters dễ dàng

## Technical Implementation

### Stack
- **Mapper**: MapStruct (BudgetMapper)
- **Repository**: Spring Data JPA + JpaSpecificationExecutor
- **Specification**: BudgetSpecification cho dynamic queries
- **Validation**: Jakarta Validation annotations
- **i18n**: MessageUtil cho error messages
- **Security**: JWT authentication

### Key Files
```
src/main/java/com/sun/expense_management/
├── controller/
│   └── BudgetController.java
├── service/
│   └── BudgetService.java
├── repository/
│   ├── BudgetRepository.java
│   └── specification/
│       └── BudgetSpecification.java
├── mapper/
│   └── BudgetMapper.java
├── dto/budget/
│   ├── BudgetRequest.java
│   ├── BudgetResponse.java
│   └── BudgetFilterRequest.java
└── entity/
    └── Budget.java
```

## Related Documentation
- [Category API Documentation](CATEGORY_API.md)
- [Income API Documentation](INCOME_API.md)
- [Expense API Documentation](README.md)
- [Specification Pattern](SPECIFICATION_PATTERN.md)
- [MapStruct Integration](MAPSTRUCT_INTEGRATION.md)
