# Income Management API (Thu nhập)

> **Lưu ý:** Tất cả các API income đều yêu cầu authentication. Thêm header `Authorization: Bearer <token>` vào mỗi request.

---

## 1. Lấy danh sách thu nhập (có phân trang và filter)

```
GET /api/incomes
```

**Query Parameters:**

| Parameter   | Type       | Default    | Description                        |
|-------------|------------|------------|------------------------------------|
| `name`      | String     | -          | Tìm kiếm theo tên thu nhập         |
| `categoryId`| Long       | -          | Lọc theo danh mục                  |
| `startDate` | LocalDate  | -          | Ngày bắt đầu (YYYY-MM-DD)          |
| `endDate`   | LocalDate  | -          | Ngày kết thúc (YYYY-MM-DD)         |
| `minAmount` | BigDecimal | -          | Số tiền tối thiểu                  |
| `maxAmount` | BigDecimal | -          | Số tiền tối đa                     |
| `page`      | Integer    | 0          | Số trang (bắt đầu từ 0)            |
| `size`      | Integer    | 10         | Số item mỗi trang                  |
| `sortBy`    | String     | incomeDate | Sắp xếp theo field                 |
| `sortDir`   | String     | desc       | Hướng sắp xếp (asc/desc)           |

**cURL:**
```bash
# Lấy danh sách thu nhập
curl -X GET "http://localhost:8080/api/incomes" \
  -H "Authorization: Bearer <token>"

# Lấy với filter
curl -X GET "http://localhost:8080/api/incomes?categoryId=2&startDate=2025-01-01&endDate=2025-12-31&page=0&size=10" \
  -H "Authorization: Bearer <token>"

# Tìm kiếm theo tên
curl -X GET "http://localhost:8080/api/incomes?name=salary" \
  -H "Authorization: Bearer <token>"
```

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "name": "Lương tháng 1",
      "amount": 15000000,
      "incomeDate": "2025-01-15",
      "note": "Lương cơ bản + thưởng",
      "source": "Công ty ABC",
      "isRecurring": true,
      "recurringType": "MONTHLY",
      "createdAt": "2025-01-15T10:00:00",
      "updatedAt": "2025-01-15T10:00:00",
      "categoryId": 2,
      "categoryName": "Lương",
      "categoryIcon": "💰"
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

## 2. Xem chi tiết thu nhập

```
GET /api/incomes/{id}
```

**cURL:**
```bash
curl -X GET "http://localhost:8080/api/incomes/1" \
  -H "Authorization: Bearer <token>"
```

**Response (200 OK):**
```json
{
  "id": 1,
  "name": "Lương tháng 1",
  "amount": 15000000,
  "incomeDate": "2025-01-15",
  "note": "Lương cơ bản + thưởng",
  "source": "Công ty ABC",
  "isRecurring": true,
  "recurringType": "MONTHLY",
  "createdAt": "2025-01-15T10:00:00",
  "updatedAt": "2025-01-15T10:00:00",
  "categoryId": 2,
  "categoryName": "Lương",
  "categoryIcon": "💰"
}
```

**Error (404 Not Found):**
```json
{
  "timestamp": "2025-01-15T10:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Không tìm thấy thu nhập với id 1",
  "path": "/api/incomes/1"
}
```

---

## 3. Tạo mới thu nhập

```
POST /api/incomes
```

**Request Body:**

| Field          | Type       | Required | Description                                    |
|----------------|------------|----------|------------------------------------------------|
| `name`         | String     | Yes      | Tên thu nhập (max 200 ký tự)                   |
| `amount`       | BigDecimal | Yes      | Số tiền (> 0)                                  |
| `incomeDate`   | LocalDate  | Yes      | Ngày thu nhập (không được ở tương lai)         |
| `categoryId`   | Long       | Yes      | ID danh mục (phải là loại INCOME)              |
| `note`         | String     | No       | Ghi chú (max 1000 ký tự)                       |
| `source`       | String     | No       | Nguồn thu nhập (max 100 ký tự)                 |
| `isRecurring`  | Boolean    | Yes      | Thu nhập định kỳ (default: false)              |
| `recurringType`| Enum       | No       | DAILY, WEEKLY, MONTHLY, YEARLY (bắt buộc nếu isRecurring=true) |

**cURL:**
```bash
curl -X POST "http://localhost:8080/api/incomes" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Lương tháng 1",
    "amount": 15000000,
    "incomeDate": "2025-01-15",
    "categoryId": 2,
    "note": "Lương cơ bản + thưởng",
    "source": "Công ty ABC",
    "isRecurring": true,
    "recurringType": "MONTHLY"
  }'
```

**Response (201 Created):**
```json
{
  "message": "Tạo thu nhập thành công",
  "data": {
    "id": 1,
    "name": "Lương tháng 1",
    "amount": 15000000,
    "incomeDate": "2025-01-15",
    "note": "Lương cơ bản + thưởng",
    "source": "Công ty ABC",
    "isRecurring": true,
    "recurringType": "MONTHLY",
    "createdAt": "2025-01-15T10:00:00",
    "updatedAt": "2025-01-15T10:00:00",
    "categoryId": 2,
    "categoryName": "Lương",
    "categoryIcon": "💰"
  }
}
```

**Error - Validation (400 Bad Request):**
```json
{
  "timestamp": "2025-01-15T10:00:00",
  "status": 400,
  "error": "Validation Failed",
  "messages": {
    "name": "Tên thu nhập không được để trống",
    "amount": "Số tiền phải lớn hơn 0",
    "incomeDate": "Ngày thu nhập không được để trống",
    "recurringType": "Loại định kỳ không được để trống khi thu nhập là định kỳ"
  }
}
```

**Error - Category không hợp lệ (400 Bad Request):**
```json
{
  "timestamp": "2025-01-15T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Danh mục không phải loại thu nhập",
  "path": "/api/incomes"
}
```

---

## 4. Cập nhật thu nhập

```
PUT /api/incomes/{id}
```

**cURL:**
```bash
curl -X PUT "http://localhost:8080/api/incomes/1" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Lương tháng 1 (đã điều chỉnh)",
    "amount": 16000000,
    "incomeDate": "2025-01-15",
    "categoryId": 2,
    "note": "Lương + thưởng hiệu suất",
    "source": "Công ty ABC",
    "isRecurring": true,
    "recurringType": "MONTHLY"
  }'
```

**Response (200 OK):**
```json
{
  "message": "Cập nhật thu nhập thành công",
  "data": {
    "id": 1,
    "name": "Lương tháng 1 (đã điều chỉnh)",
    "amount": 16000000,
    "incomeDate": "2025-01-15",
    "note": "Lương + thưởng hiệu suất",
    "source": "Công ty ABC",
    "isRecurring": true,
    "recurringType": "MONTHLY",
    "createdAt": "2025-01-15T10:00:00",
    "updatedAt": "2025-01-15T11:30:00",
    "categoryId": 2,
    "categoryName": "Lương",
    "categoryIcon": "💰"
  }
}
```

---

## 5. Xóa thu nhập

```
DELETE /api/incomes/{id}
```

**cURL:**
```bash
curl -X DELETE "http://localhost:8080/api/incomes/1" \
  -H "Authorization: Bearer <token>"
```

**Response (200 OK):**
```json
{
  "message": "Xóa thu nhập thành công"
}
```

**Error (404 Not Found):**
```json
{
  "timestamp": "2025-01-15T10:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Không tìm thấy thu nhập với id 1",
  "path": "/api/incomes/1"
}
```

---

## Error Responses chung

**401 Unauthorized (Chưa đăng nhập hoặc token hết hạn):**
```json
{
  "timestamp": "2025-01-15T10:00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource"
}
```

**403 Forbidden (Không có quyền truy cập):**
```json
{
  "timestamp": "2025-01-15T10:00:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Bạn không có quyền truy cập tài nguyên này"
}
```

---

## Ghi chú

### Recurring Types
- `DAILY` - Thu nhập hàng ngày
- `WEEKLY` - Thu nhập hàng tuần
- `MONTHLY` - Thu nhập hàng tháng
- `YEARLY` - Thu nhập hàng năm

### Category Type
- Danh mục phải có `type = INCOME`
- Sử dụng danh mục loại `EXPENSE` sẽ gây lỗi validation

### Search và Filter
- Tìm kiếm theo `name` sử dụng prefix matching (tìm từ đầu chuỗi) để tối ưu hiệu suất
- Có thể kết hợp nhiều filter cùng lúc
- Date range filter bao gồm cả `startDate` và `endDate`

### Internationalization (i18n)
API hỗ trợ đa ngôn ngữ thông qua header `Accept-Language`:
- `vi` (Tiếng Việt - mặc định)
- `en` (English)

**Ví dụ:**
```bash
curl -X GET "http://localhost:8080/api/incomes" \
  -H "Authorization: Bearer <token>" \
  -H "Accept-Language: en"
```

Hoặc sử dụng query parameter:
```bash
GET /api/incomes?lang=en
```
