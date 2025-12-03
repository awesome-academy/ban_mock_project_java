# ban_mock_project_java

## Configuration profiles (dev / prod)

This project uses YAML configuration files to separate environments.

- `src/main/resources/application.yml` — base config and default active profile (`dev`).
- Profile-specific settings are included in the same `application.yml` document under `---` sections for `dev` and `prod`.

To run the application with the `dev` profile (default):

```bash
./mvnw spring-boot:run
```

To run with the `prod` profile:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

JWT settings are located under the `app.jwt` keys in the YAML. Replace the `secret` value with a secure secret in production (or supply via environment variable management).

---

## Client Authentication - API Login

### Endpoint

```
POST /api/auth/login
```

### Request

**Headers:**
```
Content-Type: application/json
```

**Body:**
```json
{
  "email": "user@example.com",
  "password": "yourpassword"
}
```

| Field      | Type   | Required | Description         |
|------------|--------|----------|---------------------|
| `email`    | String | Yes      | Email của người dùng |
| `password` | String | Yes      | Mật khẩu            |

### Response

**Success (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Error (401 Unauthorized):**
```json
{
  "error": "Invalid credentials"
}
```

### Sử dụng Token

Sau khi login thành công, sử dụng token trong header cho các request tiếp theo:

```
Authorization: Bearer <your_token>
```

### Ví dụ với cURL

```bash
# Login để lấy token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "user@example.com", "password": "password"}'

```

---

## Expense Management API (Chi tiêu)

> **Lưu ý:** Tất cả các API expense đều yêu cầu authentication. Thêm header `Authorization: Bearer <token>` vào mỗi request.

### 1. Lấy danh sách chi tiêu (có phân trang và filter)

```
GET /api/expenses
```

**Query Parameters:**

| Parameter   | Type       | Default      | Description                        |
|-------------|------------|--------------|------------------------------------||
| `name`      | String     | -            | Tìm kiếm theo tên chi tiêu         |
| `categoryId`| Long       | -            | Lọc theo danh mục                  |
| `startDate` | LocalDate  | -            | Ngày bắt đầu (YYYY-MM-DD)          |
| `endDate`   | LocalDate  | -            | Ngày kết thúc (YYYY-MM-DD)         |
| `minAmount` | BigDecimal | -            | Số tiền tối thiểu                  |
| `maxAmount` | BigDecimal | -            | Số tiền tối đa                     |
| `page`      | Integer    | 0            | Số trang (bắt đầu từ 0)            |
| `size`      | Integer    | 10           | Số item mỗi trang                  |
| `sortBy`    | String     | expenseDate  | Sắp xếp theo field                 |
| `sortDir`   | String     | desc         | Hướng sắp xếp (asc/desc)           |

**cURL:**
```bash
# Lấy danh sách chi tiêu
curl -X GET "http://localhost:8080/api/expenses" \
  -H "Authorization: Bearer <token>"

# Lấy với filter
curl -X GET "http://localhost:8080/api/expenses?categoryId=1&startDate=2025-01-01&endDate=2025-12-31&page=0&size=10" \
  -H "Authorization: Bearer <token>"

# Tìm kiếm theo tên
curl -X GET "http://localhost:8080/api/expenses?name=coffee" \
  -H "Authorization: Bearer <token>"
```

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "name": "Cà phê sáng",
      "amount": 35000,
      "expenseDate": "2025-12-03",
      "note": "Highland Coffee",
      "location": "Quận 1",
      "paymentMethod": "CASH",
      "isRecurring": false,
      "recurringType": null,
      "createdAt": "2025-12-03T10:00:00",
      "updatedAt": "2025-12-03T10:00:00",
      "categoryId": 1,
      "categoryName": "Ăn uống",
      "categoryIcon": "🍔"
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

---

### 2. Xem chi tiết chi tiêu

```
GET /api/expenses/{id}
```

**cURL:**
```bash
curl -X GET "http://localhost:8080/api/expenses/1" \
  -H "Authorization: Bearer <token>"
```

**Response (200 OK):**
```json
{
  "id": 1,
  "name": "Cà phê sáng",
  "amount": 35000,
  "expenseDate": "2025-12-03",
  "note": "Highland Coffee",
  "location": "Quận 1",
  "paymentMethod": "CASH",
  "isRecurring": false,
  "recurringType": null,
  "createdAt": "2025-12-03T10:00:00",
  "updatedAt": "2025-12-03T10:00:00",
  "categoryId": 1,
  "categoryName": "Ăn uống",
  "categoryIcon": "🍔"
}
```

**Error (404 Not Found):**
```json
{
  "timestamp": "2025-12-03T10:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Không tìm thấy chi tiêu với id: 1"
}
```

---

### 3. Tạo mới chi tiêu

```
POST /api/expenses
```

**Request Body:**

| Field          | Type       | Required | Description                                    |
|----------------|------------|----------|------------------------------------------------|
| `name`         | String     | Yes      | Tên chi tiêu (max 200 ký tự)                   |
| `amount`       | BigDecimal | Yes      | Số tiền (> 0)                                  |
| `expenseDate`  | LocalDate  | Yes      | Ngày chi tiêu (không được ở tương lai)         |
| `categoryId`   | Long       | Yes      | ID danh mục                                    |
| `note`         | String     | No       | Ghi chú (max 1000 ký tự)                       |
| `location`     | String     | No       | Địa điểm (max 100 ký tự)                       |
| `paymentMethod`| Enum       | No       | CASH, CREDIT_CARD, DEBIT_CARD, BANK_TRANSFER, E_WALLET, OTHER |
| `isRecurring`  | Boolean    | No       | Chi tiêu định kỳ (default: false)              |
| `recurringType`| Enum       | No       | DAILY, WEEKLY, MONTHLY, YEARLY                 |

**cURL:**
```bash
curl -X POST "http://localhost:8080/api/expenses" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Cà phê sáng",
    "amount": 35000,
    "expenseDate": "2025-12-03",
    "categoryId": 1,
    "note": "Highland Coffee",
    "location": "Quận 1",
    "paymentMethod": "CASH"
  }'
```

**Response (201 Created):**
```json
{
  "id": 1,
  "name": "Cà phê sáng",
  "amount": 35000,
  "expenseDate": "2025-12-03",
  "note": "Highland Coffee",
  "location": "Quận 1",
  "paymentMethod": "CASH",
  "isRecurring": false,
  "recurringType": null,
  "createdAt": "2025-12-03T10:00:00",
  "updatedAt": "2025-12-03T10:00:00",
  "categoryId": 1,
  "categoryName": "Ăn uống",
  "categoryIcon": "🍔"
}
```

**Error - Validation (400 Bad Request):**
```json
{
  "timestamp": "2025-12-03T10:00:00",
  "status": 400,
  "error": "Validation Failed",
  "messages": {
    "name": "Tên chi tiêu không được để trống",
    "amount": "Số tiền phải lớn hơn 0",
    "expenseDate": "Ngày chi tiêu không được để trống"
  }
}
```

---

### 4. Cập nhật chi tiêu

```
PUT /api/expenses/{id}
```

**cURL:**
```bash
curl -X PUT "http://localhost:8080/api/expenses/1" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Cà phê chiều",
    "amount": 45000,
    "expenseDate": "2025-12-03",
    "categoryId": 1,
    "note": "Starbucks",
    "location": "Quận 3",
    "paymentMethod": "E_WALLET"
  }'
```

**Response (200 OK):** Giống response tạo mới

---

### 5. Xóa chi tiêu

```
DELETE /api/expenses/{id}
```

**cURL:**
```bash
gst
```

**Response (204 No Content):** Không có body

---

### Error Responses chung

**401 Unauthorized (Chưa đăng nhập hoặc token hết hạn):**
```json
{
  "timestamp": "2025-12-03T10:00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource"
}
```

**403 Forbidden (Không có quyền truy cập):**
```json
{
  "timestamp": "2025-12-03T10:00:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Bạn không có quyền truy cập tài nguyên này"
}
```
