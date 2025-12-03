# Expense Management System

## 📋 Mục lục

1. [Tổng quan](#1-tổng-quan)
2. [Client - Người dùng](#2-client---người-dùng)
3. [Admin - Quản trị viên](#3-admin---quản-trị-viên)
4. [Import / Export](#4-import--export)

---

## 1. Tổng quan

### 1.1. Thiết kế Database & GUI

| Hạng mục | Mô tả |
|----------|-------|
| **DB Schema** | Thiết kế cấu trúc cơ sở dữ liệu |
| **GUI Design** | Thiết kế giao diện người dùng |

### 1.2. Layout hệ thống

- **Client Layout**: Giao diện dành cho người dùng
- **Admin Layout**: Giao diện quản trị (namespace: `/admin`)

### 1.3. Homepage (Framework Setup)

- [ ] Khởi tạo framework
- [ ] Tạo layout cho trang chủ
- [ ] Client layout
- [ ] Admin layout (namespace admin)

---

## 2. Client - Người dùng

### 2.1. 🔐 Authentication (Xác thực)

| Chức năng | Mô tả |
|-----------|-------|
| Sign In | Đăng nhập hệ thống |
| Sign Out | Đăng xuất hệ thống |

---

### 2.2. 📊 Dashboard (Trang tổng quan)

| Thành phần | Mô tả |
|------------|-------|
| Tổng chi tiêu | Hiển thị tổng chi tiêu tháng hiện tại |
| Biểu đồ thống kê | Biểu đồ chi tiêu theo danh mục (category) |
| Tổng quan tài chính | Tổng thu nhập – Tổng chi tiêu – Số dư còn lại |
| Quick Action | Nút nhanh "Add new expense" |

---

### 2.3. 💸 Expense Management (Quản lý chi tiêu)

#### 2.3.1. List All Expenses (Index Page)

| Tính năng | Mô tả |
|-----------|-------|
| Danh sách | Hiển thị danh sách các khoản chi tiêu |
| Pagination | Phân trang |
| Search | Tìm kiếm |
| Filter | Lọc theo ngày, danh mục, số tiền |
| Navigation | Click vào khoản chi tiêu → Trang chi tiết |

#### 2.3.2. Expense Detail Page

Hiển thị thông tin chi tiết:

| Trường | Mô tả |
|--------|-------|
| Tên | Tên khoản chi tiêu |
| Số tiền | Amount |
| Ngày | Ngày chi tiêu |
| Danh mục | Category |
| Ghi chú | Note |
| File đính kèm | Hóa đơn, biên lai, ảnh chụp, v.v. |

#### 2.3.3. CRUD Operations

- [ ] **Create**: Tạo mới khoản chi tiêu
- [ ] **Update**: Sửa thông tin chi tiêu
- [ ] **Delete**: Xóa chi tiêu

---

### 2.4. 💰 Income Management (Quản lý thu nhập)

#### 2.4.1. List All Incomes (Index Page)

| Tính năng | Mô tả |
|-----------|-------|
| Danh sách | Hiển thị danh sách các nguồn thu nhập |
| Pagination | Phân trang |
| Search | Tìm kiếm |
| Filter | Lọc theo tháng, loại thu nhập |

#### 2.4.2. CRUD Operations

- [ ] **Create**: Tạo mới nguồn thu nhập
- [ ] **Update**: Sửa thông tin thu nhập
- [ ] **Delete**: Xóa thu nhập

---

### 2.5. 📂 Category Management (Quản lý danh mục)

#### 2.5.1. List All Categories

- Hiển thị danh sách các danh mục chi tiêu

#### 2.5.2. CRUD Operations

- [ ] **Create**: Thêm mới danh mục (tên, mô tả, biểu tượng)
- [ ] **Update**: Sửa danh mục
- [ ] **Delete**: Xóa danh mục

---

### 2.6. 📈 Budget Management (Quản lý ngân sách)

#### 2.6.1. List All Budgets

- Hiển thị danh sách ngân sách theo tháng hoặc danh mục

#### 2.6.2. CRUD Operations

- [ ] **Create**: Tạo mới ngân sách
- [ ] **Update**: Sửa ngân sách
- [ ] **Delete**: Xóa ngân sách

#### 2.6.3. Tính năng đặc biệt

- ⚠️ **Cảnh báo**: Thông báo khi chi tiêu vượt quá ngân sách

---

### 2.7. 📉 Report & Analytics (Báo cáo & Phân tích)

| Loại báo cáo | Mô tả |
|--------------|-------|
| Báo cáo theo thời gian | Chi tiêu theo tháng, quý, năm |
| Biểu đồ phân bố | Chi tiêu theo danh mục |
| So sánh | Thu nhập vs Chi tiêu |
| Xu hướng | Phân tích xu hướng chi tiêu theo thời gian |

---

## 3. Admin - Quản trị viên

### 3.1. 🔐 Authentication

| Chức năng | Mô tả |
|-----------|-------|
| Sign In | Đăng nhập hệ thống quản trị |
| Sign Out | Đăng xuất hệ thống quản trị |

---

### 3.2. 👥 User Management (Quản lý người dùng)

#### Danh sách & Chi tiết

| Tính năng | Mô tả |
|-----------|-------|
| List Users | Danh sách người dùng (pagination, filter theo trạng thái) |
| View Profile | Xem hồ sơ người dùng |

#### CRUD Operations

| Thao tác | Chi tiết |
|----------|----------|
| **Create** | Tạo mới: name, email, role, active status |
| **Update** | Sửa thông tin người dùng |
| **Delete** | Xóa người dùng |

---

### 3.3. 📂 Category Management

#### Danh sách

- Danh sách các danh mục chi tiêu/thu nhập

#### CRUD Operations

| Thao tác | Chi tiết |
|----------|----------|
| **Create** | Tạo mới: name, description, type (expense/income) |
| **Update** | Sửa danh mục |
| **Delete** | Xóa danh mục |

---

### 3.4. 📋 Budget Template Management

#### Danh sách

- Danh sách mẫu ngân sách (budget templates)

#### CRUD Operations

| Thao tác | Chi tiết |
|----------|----------|
| **Create** | Tạo mới: name, month, default categories & amounts |
| **Update** | Sửa template |
| **Delete** | Xóa template |

---

### 3.5. 💸 Expense Management (Admin)

| Tính năng | Mô tả |
|-----------|-------|
| List | Danh sách tất cả chi tiêu (pagination, filter theo user, category, date range) |
| View | Xem chi tiết chi tiêu |
| Edit | Chỉnh sửa chi tiêu |
| Delete | Xóa chi tiêu |

---

### 3.6. 💰 Income Management (Admin)

| Tính năng | Mô tả |
|-----------|-------|
| List | Danh sách tất cả thu nhập (pagination, filter theo user, date range) |
| Edit | Chỉnh sửa thu nhập |
| Delete | Xóa thu nhập |

---

### 3.7. 📝 Activity Log

#### Ghi nhận hoạt động

| Loại hoạt động | Mô tả |
|----------------|-------|
| Authentication | Đăng nhập/đăng xuất (user và admin) |
| CRUD Operations | Tạo, cập nhật, xóa expense, income, category, budget, user |

#### Thông tin Log

| Trường | Mô tả |
|--------|-------|
| Thời gian | Timestamp của hành động |
| Hành động | Loại action (create, update, delete, login, logout) |
| Người thực hiện | User/Admin thực hiện |
| Mô tả | Chi tiết hành động |

#### Quản lý Log

- [ ] Xem danh sách log
- [ ] Xóa log

---

## 4. Import / Export

### 4.1. 📤 Export to CSV

| Đối tượng | Dữ liệu xuất |
|-----------|--------------|
| **User** | Thông tin tổng quan, tổng chi tiêu, tổng thu nhập |
| **Expense** | Category, amount, date, note |
| **Income** | Thông tin thu nhập |
| **Category** | Danh sách danh mục |
| **Budget** | Thông tin ngân sách |

### 4.2. 📥 Import from CSV

| Đối tượng | Mô tả |
|-----------|-------|
| **User** | Import danh sách người dùng |
| **Expense** | Import danh sách chi tiêu |
| **Income** | Import danh sách thu nhập |
| **Category** | Import danh sách danh mục |
| **Budget** | Import danh sách ngân sách |

---

## 📌 Ghi chú

- **Namespace Admin**: Tất cả các route admin sử dụng prefix `/admin`
- **Pagination**: Mặc định 10-20 items/page
- **Authentication**: Sử dụng JWT hoặc Session-based authentication
- **File Upload**: Hỗ trợ upload hóa đơn, biên lai (jpg, png, pdf)
