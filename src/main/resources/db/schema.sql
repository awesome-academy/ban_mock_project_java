-- =============================================
-- EXPENSE MANAGEMENT SYSTEM - DATABASE SCHEMA
-- =============================================
-- Hệ thống quản lý chi tiêu cá nhân
-- Created: 2024
-- =============================================

-- Tạo database
CREATE DATABASE IF NOT EXISTS expense_management
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE expense_management;

-- =============================================
-- 1. BẢNG USERS (Người dùng)
-- =============================================
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT 'Họ và tên',
    email VARCHAR(150) NOT NULL UNIQUE COMMENT 'Email đăng nhập',
    password VARCHAR(255) NOT NULL COMMENT 'Mật khẩu (đã mã hóa)',
    phone VARCHAR(20) COMMENT 'Số điện thoại',
    avatar TEXT COMMENT 'Đường dẫn ảnh đại diện',
    role ENUM('USER', 'ADMIN') NOT NULL DEFAULT 'USER' COMMENT 'Vai trò',
    active BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Trạng thái hoạt động',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_users_email (email),
    INDEX idx_users_role (role),
    INDEX idx_users_active (active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Bảng người dùng';

-- =============================================
-- 2. BẢNG CATEGORIES (Danh mục)
-- =============================================
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT 'Tên danh mục',
    description VARCHAR(255) COMMENT 'Mô tả',
    icon VARCHAR(50) COMMENT 'Icon (emoji hoặc icon class)',
    color VARCHAR(20) COMMENT 'Màu sắc (hex code)',
    type ENUM('EXPENSE', 'INCOME') NOT NULL DEFAULT 'EXPENSE' COMMENT 'Loại danh mục',
    active BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Trạng thái',
    is_default BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Danh mục mặc định (system)',
    user_id BIGINT COMMENT 'NULL = danh mục hệ thống, có giá trị = danh mục của user',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_categories_type (type),
    INDEX idx_categories_user (user_id),
    INDEX idx_categories_active (active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Bảng danh mục chi tiêu/thu nhập';

-- =============================================
-- 3. BẢNG EXPENSES (Chi tiêu)
-- =============================================
CREATE TABLE IF NOT EXISTS expenses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL COMMENT 'Tên khoản chi tiêu',
    amount DECIMAL(15, 2) NOT NULL COMMENT 'Số tiền',
    expense_date DATE NOT NULL COMMENT 'Ngày chi tiêu',
    note TEXT COMMENT 'Ghi chú',
    location VARCHAR(100) COMMENT 'Địa điểm',
    payment_method ENUM('CASH', 'CREDIT_CARD', 'DEBIT_CARD', 'BANK_TRANSFER', 'E_WALLET', 'OTHER')
        DEFAULT 'CASH' COMMENT 'Phương thức thanh toán',
    is_recurring BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Chi tiêu định kỳ',
    recurring_type ENUM('DAILY', 'WEEKLY', 'MONTHLY', 'YEARLY') COMMENT 'Loại định kỳ',
    user_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT,
    INDEX idx_expenses_user (user_id),
    INDEX idx_expenses_category (category_id),
    INDEX idx_expenses_date (expense_date),
    INDEX idx_expenses_amount (amount)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Bảng chi tiêu';

-- =============================================
-- 4. BẢNG INCOMES (Thu nhập)
-- =============================================
CREATE TABLE IF NOT EXISTS incomes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL COMMENT 'Tên nguồn thu nhập',
    amount DECIMAL(15, 2) NOT NULL COMMENT 'Số tiền',
    income_date DATE NOT NULL COMMENT 'Ngày thu nhập',
    note TEXT COMMENT 'Ghi chú',
    source VARCHAR(100) COMMENT 'Nguồn thu nhập',
    is_recurring BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Thu nhập định kỳ',
    recurring_type ENUM('DAILY', 'WEEKLY', 'MONTHLY', 'YEARLY') COMMENT 'Loại định kỳ',
    user_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT,
    INDEX idx_incomes_user (user_id),
    INDEX idx_incomes_category (category_id),
    INDEX idx_incomes_date (income_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Bảng thu nhập';

-- =============================================
-- 5. BẢNG BUDGETS (Ngân sách)
-- =============================================
CREATE TABLE IF NOT EXISTS budgets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL COMMENT 'Tên ngân sách',
    amount_limit DECIMAL(15, 2) NOT NULL COMMENT 'Hạn mức ngân sách',
    spent_amount DECIMAL(15, 2) NOT NULL DEFAULT 0 COMMENT 'Số tiền đã chi',
    year INT NOT NULL COMMENT 'Năm',
    month INT NOT NULL COMMENT 'Tháng (1-12)',
    note TEXT COMMENT 'Ghi chú',
    alert_threshold INT DEFAULT 80 COMMENT 'Ngưỡng cảnh báo (%)',
    is_alert_sent BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Đã gửi cảnh báo chưa',
    active BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Trạng thái',
    user_id BIGINT NOT NULL,
    category_id BIGINT COMMENT 'NULL = ngân sách tổng, có giá trị = ngân sách theo danh mục',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
    INDEX idx_budgets_user (user_id),
    INDEX idx_budgets_category (category_id),
    INDEX idx_budgets_period (year, month),
    UNIQUE KEY uk_budget_user_category_period (user_id, category_id, year, month)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Bảng ngân sách';

-- =============================================
-- 6. BẢNG BUDGET_TEMPLATES (Mẫu ngân sách - Admin)
-- =============================================
CREATE TABLE IF NOT EXISTS budget_templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL COMMENT 'Tên mẫu ngân sách',
    description TEXT COMMENT 'Mô tả',
    active BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Trạng thái',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_templates_active (active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Bảng mẫu ngân sách';

-- =============================================
-- 7. BẢNG BUDGET_TEMPLATE_ITEMS (Chi tiết mẫu ngân sách)
-- =============================================
CREATE TABLE IF NOT EXISTS budget_template_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    default_amount DECIMAL(15, 2) NOT NULL COMMENT 'Số tiền mặc định',
    budget_template_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (budget_template_id) REFERENCES budget_templates(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE,
    UNIQUE KEY uk_template_category (budget_template_id, category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Chi tiết mẫu ngân sách';

-- =============================================
-- 8. BẢNG ATTACHMENTS (File đính kèm)
-- =============================================
CREATE TABLE IF NOT EXISTS attachments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL COMMENT 'Tên file',
    file_path TEXT NOT NULL COMMENT 'Đường dẫn lưu trữ',
    file_type VARCHAR(100) COMMENT 'Loại file (MIME type)',
    file_size BIGINT COMMENT 'Kích thước file (bytes)',
    expense_id BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (expense_id) REFERENCES expenses(id) ON DELETE CASCADE,
    INDEX idx_attachments_expense (expense_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Bảng file đính kèm';

-- =============================================
-- 9. BẢNG ACTIVITY_LOGS (Nhật ký hoạt động)
-- =============================================
CREATE TABLE IF NOT EXISTS activity_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    action ENUM('LOGIN', 'LOGOUT', 'CREATE', 'UPDATE', 'DELETE', 'EXPORT', 'IMPORT', 'VIEW')
        NOT NULL COMMENT 'Loại hành động',
    entity_type VARCHAR(50) NOT NULL COMMENT 'Loại đối tượng (User, Expense, Income, etc.)',
    entity_id BIGINT COMMENT 'ID của đối tượng',
    description TEXT COMMENT 'Mô tả chi tiết',
    old_value TEXT COMMENT 'Giá trị cũ (JSON)',
    new_value TEXT COMMENT 'Giá trị mới (JSON)',
    ip_address VARCHAR(50) COMMENT 'Địa chỉ IP',
    user_agent TEXT COMMENT 'Thông tin trình duyệt',
    user_id BIGINT COMMENT 'Người thực hiện',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_logs_user (user_id),
    INDEX idx_logs_action (action),
    INDEX idx_logs_entity (entity_type, entity_id),
    INDEX idx_logs_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Bảng nhật ký hoạt động';

-- =============================================
-- DỮ LIỆU MẪU
-- =============================================

-- Tạo admin mặc định (password: admin123 - cần hash trong thực tế)
INSERT INTO users (name, email, password, role, active) VALUES
('Administrator', 'admin@expense.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'ADMIN', TRUE);

-- Tạo danh mục chi tiêu mặc định
INSERT INTO categories (name, description, icon, color, type, is_default) VALUES
-- Danh mục chi tiêu
('Ăn uống', 'Chi phí ăn uống hàng ngày', '🍔', '#FF6B6B', 'EXPENSE', TRUE),
('Di chuyển', 'Chi phí đi lại, xăng xe', '🚗', '#4ECDC4', 'EXPENSE', TRUE),
('Mua sắm', 'Chi phí mua sắm quần áo, đồ dùng', '🛒', '#45B7D1', 'EXPENSE', TRUE),
('Giải trí', 'Chi phí giải trí, xem phim, du lịch', '🎬', '#96CEB4', 'EXPENSE', TRUE),
('Hóa đơn', 'Điện, nước, internet, điện thoại', '📄', '#FFEAA7', 'EXPENSE', TRUE),
('Y tế', 'Chi phí khám bệnh, thuốc men', '🏥', '#DDA0DD', 'EXPENSE', TRUE),
('Giáo dục', 'Học phí, sách vở, khóa học', '📚', '#98D8C8', 'EXPENSE', TRUE),
('Nhà ở', 'Tiền thuê nhà, sửa chữa', '🏠', '#F7DC6F', 'EXPENSE', TRUE),
('Khác', 'Các khoản chi tiêu khác', '📦', '#BDC3C7', 'EXPENSE', TRUE),

-- Danh mục thu nhập
('Lương', 'Thu nhập từ lương hàng tháng', '💰', '#2ECC71', 'INCOME', TRUE),
('Thưởng', 'Tiền thưởng, bonus', '🎁', '#27AE60', 'INCOME', TRUE),
('Đầu tư', 'Thu nhập từ đầu tư, lãi suất', '📈', '#1ABC9C', 'INCOME', TRUE),
('Kinh doanh', 'Thu nhập từ kinh doanh phụ', '💼', '#3498DB', 'INCOME', TRUE),
('Quà tặng', 'Tiền được tặng, biếu', '🎀', '#E74C3C', 'INCOME', TRUE),
('Khác', 'Các nguồn thu nhập khác', '💵', '#95A5A6', 'INCOME', TRUE);

-- =============================================
-- VIEWS HỮU ÍCH
-- =============================================

-- View tổng hợp chi tiêu theo tháng của user
CREATE OR REPLACE VIEW vw_monthly_expense_summary AS
SELECT
    u.id AS user_id,
    u.name AS user_name,
    YEAR(e.expense_date) AS year,
    MONTH(e.expense_date) AS month,
    c.name AS category_name,
    SUM(e.amount) AS total_amount,
    COUNT(e.id) AS transaction_count
FROM expenses e
JOIN users u ON e.user_id = u.id
JOIN categories c ON e.category_id = c.id
GROUP BY u.id, u.name, YEAR(e.expense_date), MONTH(e.expense_date), c.id, c.name;

-- View tổng hợp thu nhập theo tháng của user
CREATE OR REPLACE VIEW vw_monthly_income_summary AS
SELECT
    u.id AS user_id,
    u.name AS user_name,
    YEAR(i.income_date) AS year,
    MONTH(i.income_date) AS month,
    c.name AS category_name,
    SUM(i.amount) AS total_amount,
    COUNT(i.id) AS transaction_count
FROM incomes i
JOIN users u ON i.user_id = u.id
JOIN categories c ON i.category_id = c.id
GROUP BY u.id, u.name, YEAR(i.income_date), MONTH(i.income_date), c.id, c.name;

-- View cân đối thu chi theo tháng
CREATE OR REPLACE VIEW vw_monthly_balance AS
SELECT
    user_id,
    year,
    month,
    total_income,
    total_expense,
    (total_income - total_expense) AS balance
FROM (
    SELECT
        u.id AS user_id,
        YEAR(COALESCE(e.expense_date, i.income_date)) AS year,
        MONTH(COALESCE(e.expense_date, i.income_date)) AS month,
        COALESCE(SUM(i.amount), 0) AS total_income,
        COALESCE(SUM(e.amount), 0) AS total_expense
    FROM users u
    LEFT JOIN expenses e ON u.id = e.user_id
    LEFT JOIN incomes i ON u.id = i.user_id
    GROUP BY u.id, YEAR(COALESCE(e.expense_date, i.income_date)), MONTH(COALESCE(e.expense_date, i.income_date))
) AS summary;

-- =============================================
-- STORED PROCEDURES
-- =============================================

DELIMITER //

-- Procedure cập nhật số tiền đã chi trong ngân sách
CREATE PROCEDURE sp_update_budget_spent_amount(IN p_user_id BIGINT, IN p_year INT, IN p_month INT)
BEGIN
    -- Cập nhật ngân sách tổng
    UPDATE budgets b
    SET b.spent_amount = (
        SELECT COALESCE(SUM(e.amount), 0)
        FROM expenses e
        WHERE e.user_id = p_user_id
        AND YEAR(e.expense_date) = p_year
        AND MONTH(e.expense_date) = p_month
    )
    WHERE b.user_id = p_user_id
    AND b.year = p_year
    AND b.month = p_month
    AND b.category_id IS NULL;

    -- Cập nhật ngân sách theo danh mục
    UPDATE budgets b
    SET b.spent_amount = (
        SELECT COALESCE(SUM(e.amount), 0)
        FROM expenses e
        WHERE e.user_id = p_user_id
        AND e.category_id = b.category_id
        AND YEAR(e.expense_date) = p_year
        AND MONTH(e.expense_date) = p_month
    )
    WHERE b.user_id = p_user_id
    AND b.year = p_year
    AND b.month = p_month
    AND b.category_id IS NOT NULL;
END //

DELIMITER ;

-- =============================================
-- TRIGGERS
-- =============================================

DELIMITER //

-- Trigger cập nhật ngân sách khi thêm chi tiêu
CREATE TRIGGER trg_expense_after_insert
AFTER INSERT ON expenses
FOR EACH ROW
BEGIN
    CALL sp_update_budget_spent_amount(NEW.user_id, YEAR(NEW.expense_date), MONTH(NEW.expense_date));
END //

-- Trigger cập nhật ngân sách khi sửa chi tiêu
CREATE TRIGGER trg_expense_after_update
AFTER UPDATE ON expenses
FOR EACH ROW
BEGIN
    -- Cập nhật tháng cũ
    IF OLD.expense_date != NEW.expense_date THEN
        CALL sp_update_budget_spent_amount(OLD.user_id, YEAR(OLD.expense_date), MONTH(OLD.expense_date));
    END IF;
    -- Cập nhật tháng mới
    CALL sp_update_budget_spent_amount(NEW.user_id, YEAR(NEW.expense_date), MONTH(NEW.expense_date));
END //

-- Trigger cập nhật ngân sách khi xóa chi tiêu
CREATE TRIGGER trg_expense_after_delete
AFTER DELETE ON expenses
FOR EACH ROW
BEGIN
    CALL sp_update_budget_spent_amount(OLD.user_id, YEAR(OLD.expense_date), MONTH(OLD.expense_date));
END //

DELIMITER ;
