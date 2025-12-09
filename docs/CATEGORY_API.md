# Category API Documentation

## Endpoints

### 1. Get Categories (List with Filters)

**Endpoint:** `GET /api/categories`

**Description:** Lấy danh sách categories với pagination và filter. User có thể thấy categories của mình + default categories.

**Headers:**
```
Authorization: Bearer <token>
```

**Query Parameters:**
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| name | String | No | - | Filter theo tên (case-insensitive, partial match) |
| type | Enum | No | - | Filter theo loại: `EXPENSE` hoặc `INCOME` |
| active | Boolean | No | - | Filter theo trạng thái active |
| page | Integer | No | 0 | Số trang (bắt đầu từ 0) |
| size | Integer | No | 10 | Số lượng items mỗi trang |
| sortBy | String | No | name | Field để sort: `name`, `type`, `createdAt` |
| sortDir | String | No | asc | Hướng sort: `asc` hoặc `desc` |

**Response:** `200 OK`
```json
{
  "content": [
    {
      "id": 1,
      "name": "Ăn uống",
      "description": "Chi tiêu cho ăn uống, nhà hàng, cafe",
      "icon": "🍔",
      "color": "#FF6B6B",
      "type": "EXPENSE",
      "active": true,
      "isDefault": true,
      "createdAt": "2025-12-03T16:06:25.667604",
      "updatedAt": "2025-12-03T16:06:25.667612",
      "userId": null,
      "userName": null
    }
  ],
  "pageNumber": 0,
  "pageSize": 10,
  "totalElements": 12,
  "totalPages": 2,
  "first": true,
  "last": false
}
```

**Examples:**
```bash
# Get all categories
curl -X GET "http://localhost:8080/api/categories" \
  -H "Authorization: Bearer <token>"

# Filter by type INCOME
curl -X GET "http://localhost:8080/api/categories?type=INCOME" \
  -H "Authorization: Bearer <token>"

# Filter by name and type
curl -X GET "http://localhost:8080/api/categories?name=Lương&type=INCOME&active=true" \
  -H "Authorization: Bearer <token>"

# With pagination and sorting
curl -X GET "http://localhost:8080/api/categories?page=0&size=5&sortBy=createdAt&sortDir=desc" \
  -H "Authorization: Bearer <token>"
```

---

### 2. Get Category by ID

**Endpoint:** `GET /api/categories/{id}`

**Description:** Lấy chi tiết một category. User chỉ có thể xem category của mình hoặc default categories.

**Headers:**
```
Authorization: Bearer <token>
```

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| id | Long | Yes | Category ID |

**Response:** `200 OK`
```json
{
  "id": 13,
  "name": "Freelance Web Dev",
  "description": "Thu nhập từ làm web freelance",
  "icon": "💻",
  "color": "#8E44AD",
  "type": "INCOME",
  "active": true,
  "isDefault": false,
  "createdAt": "2025-12-09T16:53:22.30395",
  "updatedAt": "2025-12-09T16:53:49.741132",
  "userId": 1,
  "userName": "Demo User"
}
```

**Error Responses:**
- `404 Not Found` - Category không tồn tại hoặc không có quyền truy cập

**Example:**
```bash
curl -X GET "http://localhost:8080/api/categories/13" \
  -H "Authorization: Bearer <token>"
```

---

### 3. Create Category

**Endpoint:** `POST /api/categories`

**Description:** Tạo category mới. Category sẽ thuộc về user đang đăng nhập.

**Headers:**
```
Authorization: Bearer <token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "Freelance",
  "description": "Thu nhập từ công việc freelance",
  "icon": "💻",
  "color": "#9B59B6",
  "type": "INCOME",
  "active": true
}
```

**Field Validations:**
| Field | Type | Required | Validation |
|-------|------|----------|------------|
| name | String | Yes | Max 100 characters |
| description | String | No | Max 255 characters |
| icon | String | No | Max 50 characters |
| color | String | No | Max 20 characters (HEX format) |
| type | Enum | Yes | `EXPENSE` hoặc `INCOME` |
| active | Boolean | No | Default: true |

**Response:** `201 Created`
```json
{
  "id": 13,
  "name": "Freelance",
  "description": "Thu nhập từ công việc freelance",
  "icon": "💻",
  "color": "#9B59B6",
  "type": "INCOME",
  "active": true,
  "isDefault": false,
  "createdAt": "2025-12-09T16:53:22.303950234",
  "updatedAt": "2025-12-09T16:53:22.303975034",
  "userId": 1,
  "userName": "Demo User"
}
```

**Error Responses:**
- `400 Bad Request` - Validation errors

**Example:**
```bash
curl -X POST "http://localhost:8080/api/categories" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Freelance",
    "description": "Thu nhập từ công việc freelance",
    "icon": "💻",
    "color": "#9B59B6",
    "type": "INCOME",
    "active": true
  }'
```

---

### 4. Update Category

**Endpoint:** `PUT /api/categories/{id}`

**Description:** Cập nhật category. User chỉ có thể update category của mình (không thể update default categories).

**Headers:**
```
Authorization: Bearer <token>
Content-Type: application/json
```

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| id | Long | Yes | Category ID |

**Request Body:**
```json
{
  "name": "Freelance Web Dev",
  "description": "Thu nhập từ làm web freelance",
  "icon": "💻",
  "color": "#8E44AD",
  "type": "INCOME",
  "active": true
}
```

**Response:** `200 OK`
```json
{
  "id": 13,
  "name": "Freelance Web Dev",
  "description": "Thu nhập từ làm web freelance",
  "icon": "💻",
  "color": "#8E44AD",
  "type": "INCOME",
  "active": true,
  "isDefault": false,
  "createdAt": "2025-12-09T16:53:22.30395",
  "updatedAt": "2025-12-09T16:53:49.741132",
  "userId": 1,
  "userName": "Demo User"
}
```

**Error Responses:**
- `404 Not Found` - Category không tồn tại hoặc không có quyền update
- `400 Bad Request` - Validation errors

**Example:**
```bash
curl -X PUT "http://localhost:8080/api/categories/13" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Freelance Web Dev",
    "description": "Thu nhập từ làm web freelance",
    "icon": "💻",
    "color": "#8E44AD",
    "type": "INCOME",
    "active": true
  }'
```

---

### 5. Delete Category (Soft Delete)

**Endpoint:** `DELETE /api/categories/{id}`

**Description:** Xóa (soft delete) category bằng cách set `active = false`. User chỉ có thể xóa category của mình (không thể xóa default categories).

**Headers:**
```
Authorization: Bearer <token>
```

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| id | Long | Yes | Category ID |

**Response:** `204 No Content`

**Error Responses:**
- `404 Not Found` - Category không tồn tại hoặc không có quyền xóa

**Example:**
```bash
curl -X DELETE "http://localhost:8080/api/categories/13" \
  -H "Authorization: Bearer eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwiaWF0IjoxNzY1MjczOTc5LCJleHAiOjE3NjUzNjAzNzl9.y76RhEogETNNXMTEOn0hMUQMBrDw7-qS0gxzrxUWpIhrffiTprxUYdkOWhBZeJCo"
```

---

## Business Logic Notes

### 1. Category Visibility Rules
- **User Categories**: User chỉ thấy categories của mình
- **Default Categories**: Tất cả users đều thấy default categories (isDefault=true)
- **Admin**: (Future) Admin có thể thấy tất cả categories

### 2. Permission Rules
- **Read**: User có thể xem categories của mình + default categories
- **Create**: User tạo category mới (tự động gán user_id)
- **Update**: User chỉ update được category của mình (không update được default)
- **Delete**: User chỉ xóa được category của mình (không xóa được default)

### 3. Soft Delete
- DELETE endpoint thực hiện soft delete (set active=false)
- Category bị xóa vẫn tồn tại trong DB nhưng ẩn khỏi user
- Có thể filter `active=false` để xem categories đã xóa

### 4. Dynamic Query with Specification Pattern
- Sử dụng JPA Specification cho dynamic queries
- Type-safe, maintainable code
- Hỗ trợ complex filters dễ dàng

## Technical Implementation

### Stack
- **Mapper**: MapStruct (CategoryMapper)
- **Repository**: Spring Data JPA + JpaSpecificationExecutor
- **Specification**: CategorySpecification cho dynamic queries
- **Validation**: Jakarta Validation annotations
- **i18n**: MessageUtil cho error messages
- **Security**: JWT authentication

### Key Files
```
src/main/java/com/sun/expense_management/
├── controller/
│   └── CategoryController.java
├── service/
│   └── CategoryService.java
├── repository/
│   ├── CategoryRepository.java
│   └── specification/
│       └── CategorySpecification.java
├── mapper/
│   └── CategoryMapper.java
├── dto/category/
│   ├── CategoryRequest.java
│   ├── CategoryResponse.java
│   └── CategoryFilterRequest.java
└── entity/
    └── Category.java
```

## Related Documentation
- [Income API Documentation](INCOME_API.md)
- [Expense API Documentation](README.md)
- [Specification Pattern](SPECIFICATION_PATTERN.md)
- [MapStruct Integration](MAPSTRUCT_INTEGRATION.md)
