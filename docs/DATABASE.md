# Database Schema - Expense Management System

## 📊 Entity Relationship Diagram (ERD)

```
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                           EXPENSE MANAGEMENT SYSTEM - ERD                                │
└─────────────────────────────────────────────────────────────────────────────────────────┘

┌──────────────┐       ┌───────────────┐       ┌──────────────────┐
│    USERS     │       │  CATEGORIES   │       │   EXPENSES       │
├──────────────┤       ├────────────── ┤       ├──────────────────┤
│ PK id        │──┐    │ PK id         │──┐    │ PK id            │
│    name      │  │    │    name       │  │    │    name          │
│    email     │  │    │    description│  │    │    amount        │
│    password  │  │    │    icon       │  │    │    expense_date  │
│    phone     │  │    │    color      │  │    │    note          │
│    avatar    │  │    │    type       │  │    │    location      │
│    role      │  │    │    active     │  │    │    payment_method│
│    active    │  │    │    is_default │  │    │    is_recurring  │
│    created_at│  │    │ FK user_id    │◄─┤    │    recurring_type│
│    updated_at│  │    │    created_at │  │    │ FK user_id       │◄──┐
└──────────────┘  │    │    updated_at │  │    │ FK category_id   │ ◄─┤
       │          │    └───────────────┘  │    │    created_at    │   │
       │          │           │           │    │    updated_at    │   │
       │          │           │           │    └──────────────────┘   │
       │          │           │           │           │               │
       │          │           │           │           │               │
       │          └───────────┼───────────┼───────────┘               │
       │                      │           │                           │
       │                      ▼           │                           │
       │          ┌──────────────────┐    │                           │
       │          │   INCOMES        │    │                           │
       │          ├──────────────────┤    │                           │
       │          │ PK id            │    │                           │
       │          │    name          │    │                           │
       │          │    amount        │    │                           │
       │          │    income_date   │    │                           │
       │          │    note          │    │                           │
       │          │    source        │    │                           │
       │          │    is_recurring  │    │                           │
       │          │    recurring_type│    │                           │
       │          │ FK user_id       │◄───┤                           │
       │          │ FK category_id   │◄───┘                           │
       │          │    created_at    │                                │
       │          │    updated_at    │                                │
       │          └──────────────────┘                                │
       │                                                              │
       │          ┌───────────────────┐  ┌──────────────┐             │
       │          │   BUDGETS         │  │ ATTACHMENTS  │             │
       │          ├───────────────────┤  ├──────────────┤             │
       │          │ PK id             │  │ PK id        │             │
       │          │    name           │  │    file_name │             │
       │          │    amount_limit   │  │    file_path │             │
       │          │    spent_amount   │  │    file_type │             │
       │          │    year           │  │    file_size │             │
       │          │    month          │  │ FK expense_id│◄────────────┘
       │          │    note           │  │    created_at│
       │          │    alert_threshold│  └──────────────┘
       │          │    is_alert_sent  │
       │          │    active         │
       │          │ FK user_id        │◄───────────────────┐
       │          │ FK category_id    │◄───────────────┐   │
       │          │    created_at     │                │   │
       │          │    updated_at     │                │   │
       │          └───────────────────┘                │   │
       │                                               │   │
       │                                               │   │
       │          ┌────────────────────┐               │   │
       │          │  BUDGET_TEMPLATES  │               │   │
       │          ├────────────────────┤               │   │
       │          │ PK id              │               │   │
       │          │    name            │───┐           │   │
       │          │    description     │   │           │   │
       │          │    active          │   │           │   │
       │          │    created_at      │   │           │   │
       │          │    updated_at      │   │           │   │
       │          └────────────────────┘   │           │   │
       │                                   │           │   │
       │          ┌────────────────────────┼───────────┘   │
       │          │                        │               │
       │          ▼                        │               │
       │  ┌──────────────────────┐         │               │
       │  │ BUDGET_TEMPLATE_ITEMS│         │               │
       │  ├──────────────────────┤         │               │
       │  │ PK id                │         │               │
       │  │    default_amount    │         │               │
       │  │ FK budget_template_id│ ◄───────┘               │
       │  │ FK category_id       │                         │
       │  │    created_at        │                         │
       │  │    updated_at        │                         │
       │  └──────────────────────┘                         │
       │                                                   │
       │          ┌──────────────────┐                     │
       │          │  ACTIVITY_LOGS   │                     │
       │          ├──────────────────┤                     │
       │          │ PK id            │                     │
       │          │    action        │                     │
       │          │    entity_type   │                     │
       │          │    entity_id     │                     │
       │          │    description   │                     │
       │          │    old_value     │                     │
       │          │    new_value     │                     │
       │          │    ip_address    │                     │
       │          │    user_agent    │                     │
       └──────────│ FK user_id       │◄────────────────────┘
                  │    created_at    │
                  └──────────────────┘
```

---

## 📋 Mô tả các bảng

### 1. `users` - Bảng người dùng

| Cột | Kiểu dữ liệu | Mô tả |
|-----|-------------|-------|
| `id` | BIGINT | Khóa chính, tự tăng |
| `name` | VARCHAR(100) | Họ và tên |
| `email` | VARCHAR(150) | Email đăng nhập (unique) |
| `password` | VARCHAR(255) | Mật khẩu đã mã hóa |
| `phone` | VARCHAR(20) | Số điện thoại |
| `avatar` | TEXT | Đường dẫn ảnh đại diện |
| `role` | ENUM | USER / ADMIN |
| `active` | BOOLEAN | Trạng thái hoạt động |
| `created_at` | DATETIME | Thời gian tạo |
| `updated_at` | DATETIME | Thời gian cập nhật |

---

### 2. `categories` - Bảng danh mục

| Cột | Kiểu dữ liệu | Mô tả |
|-----|-------------|-------|
| `id` | BIGINT | Khóa chính |
| `name` | VARCHAR(100) | Tên danh mục |
| `description` | VARCHAR(255) | Mô tả |
| `icon` | VARCHAR(50) | Icon (emoji hoặc class) |
| `color` | VARCHAR(20) | Màu sắc (hex) |
| `type` | ENUM | EXPENSE / INCOME |
| `active` | BOOLEAN | Trạng thái |
| `is_default` | BOOLEAN | Danh mục hệ thống |
| `user_id` | BIGINT (FK) | NULL = hệ thống, có giá trị = của user |

---

### 3. `expenses` - Bảng chi tiêu

| Cột | Kiểu dữ liệu | Mô tả |
|-----|-------------|-------|
| `id` | BIGINT | Khóa chính |
| `name` | VARCHAR(200) | Tên khoản chi |
| `amount` | DECIMAL(15,2) | Số tiền |
| `expense_date` | DATE | Ngày chi tiêu |
| `note` | TEXT | Ghi chú |
| `location` | VARCHAR(100) | Địa điểm |
| `payment_method` | ENUM | CASH, CREDIT_CARD, DEBIT_CARD, BANK_TRANSFER, E_WALLET, OTHER |
| `is_recurring` | BOOLEAN | Chi tiêu định kỳ |
| `recurring_type` | ENUM | DAILY, WEEKLY, MONTHLY, YEARLY |
| `user_id` | BIGINT (FK) | Người dùng |
| `category_id` | BIGINT (FK) | Danh mục |

---

### 4. `incomes` - Bảng thu nhập

| Cột | Kiểu dữ liệu | Mô tả |
|-----|-------------|-------|
| `id` | BIGINT | Khóa chính |
| `name` | VARCHAR(200) | Tên nguồn thu |
| `amount` | DECIMAL(15,2) | Số tiền |
| `income_date` | DATE | Ngày thu nhập |
| `note` | TEXT | Ghi chú |
| `source` | VARCHAR(100) | Nguồn thu nhập |
| `is_recurring` | BOOLEAN | Thu nhập định kỳ |
| `recurring_type` | ENUM | DAILY, WEEKLY, MONTHLY, YEARLY |
| `user_id` | BIGINT (FK) | Người dùng |
| `category_id` | BIGINT (FK) | Danh mục |

---

### 5. `budgets` - Bảng ngân sách

| Cột | Kiểu dữ liệu | Mô tả |
|-----|-------------|-------|
| `id` | BIGINT | Khóa chính |
| `name` | VARCHAR(200) | Tên ngân sách |
| `amount_limit` | DECIMAL(15,2) | Hạn mức |
| `spent_amount` | DECIMAL(15,2) | Đã chi |
| `year` | INT | Năm |
| `month` | INT | Tháng (1-12) |
| `note` | TEXT | Ghi chú |
| `alert_threshold` | INT | Ngưỡng cảnh báo (%) |
| `is_alert_sent` | BOOLEAN | Đã gửi cảnh báo |
| `active` | BOOLEAN | Trạng thái |
| `user_id` | BIGINT (FK) | Người dùng |
| `category_id` | BIGINT (FK) | NULL = tổng, có giá trị = theo danh mục |

---

### 6. `budget_templates` - Mẫu ngân sách (Admin)

| Cột | Kiểu dữ liệu | Mô tả |
|-----|-------------|-------|
| `id` | BIGINT | Khóa chính |
| `name` | VARCHAR(200) | Tên mẫu |
| `description` | TEXT | Mô tả |
| `active` | BOOLEAN | Trạng thái |

---

### 7. `budget_template_items` - Chi tiết mẫu ngân sách

| Cột | Kiểu dữ liệu | Mô tả |
|-----|-------------|-------|
| `id` | BIGINT | Khóa chính |
| `default_amount` | DECIMAL(15,2) | Số tiền mặc định |
| `budget_template_id` | BIGINT (FK) | Mẫu ngân sách |
| `category_id` | BIGINT (FK) | Danh mục |

---

### 8. `attachments` - File đính kèm

| Cột | Kiểu dữ liệu | Mô tả |
|-----|-------------|-------|
| `id` | BIGINT | Khóa chính |
| `file_name` | VARCHAR(255) | Tên file |
| `file_path` | TEXT | Đường dẫn lưu trữ |
| `file_type` | VARCHAR(100) | MIME type |
| `file_size` | BIGINT | Kích thước (bytes) |
| `expense_id` | BIGINT (FK) | Khoản chi tiêu |

---

### 9. `activity_logs` - Nhật ký hoạt động

| Cột | Kiểu dữ liệu | Mô tả |
|-----|-------------|-------|
| `id` | BIGINT | Khóa chính |
| `action` | ENUM | LOGIN, LOGOUT, CREATE, UPDATE, DELETE, EXPORT, IMPORT, VIEW |
| `entity_type` | VARCHAR(50) | Loại đối tượng |
| `entity_id` | BIGINT | ID đối tượng |
| `description` | TEXT | Mô tả |
| `old_value` | TEXT | Giá trị cũ (JSON) |
| `new_value` | TEXT | Giá trị mới (JSON) |
| `ip_address` | VARCHAR(50) | Địa chỉ IP |
| `user_agent` | TEXT | Trình duyệt |
| `user_id` | BIGINT (FK) | Người thực hiện |

---

## 🔗 Quan hệ giữa các bảng

| Bảng nguồn | Quan hệ | Bảng đích | Mô tả |
|------------|---------|-----------|-------|
| `users` | 1:N | `expenses` | Một user có nhiều chi tiêu |
| `users` | 1:N | `incomes` | Một user có nhiều thu nhập |
| `users` | 1:N | `budgets` | Một user có nhiều ngân sách |
| `users` | 1:N | `categories` | Một user có thể tạo nhiều danh mục riêng |
| `users` | 1:N | `activity_logs` | Một user có nhiều log hoạt động |
| `categories` | 1:N | `expenses` | Một danh mục có nhiều chi tiêu |
| `categories` | 1:N | `incomes` | Một danh mục có nhiều thu nhập |
| `categories` | 1:N | `budgets` | Một danh mục có nhiều ngân sách |
| `expenses` | 1:N | `attachments` | Một chi tiêu có nhiều file đính kèm |
| `budget_templates` | 1:N | `budget_template_items` | Một mẫu có nhiều chi tiết |

---

## 📁 Cấu trúc files

```
src/main/java/com/sun/expense_management/
├── entity/
│   ├── User.java
│   ├── Category.java
│   ├── Expense.java
│   ├── Income.java
│   ├── Budget.java
│   ├── BudgetTemplate.java
│   ├── BudgetTemplateItem.java
│   ├── Attachment.java
│   └── ActivityLog.java
└── ...

src/main/resources/
├── application.properties
└── db/
    └── schema.sql
```
