# I18n Configuration Guide

## Overview
Hệ thống hỗ trợ đa ngôn ngữ (i18n) với 2 ngôn ngữ:
- 🇻🇳 **Tiếng Việt (vi)** - Mặc định
- 🇺🇸 **English (en)**

## Cách sử dụng

### 1. Đổi ngôn ngữ qua HTTP Header
```bash
# Vietnamese (default)
curl -H "Accept-Language: vi" http://localhost:8080/api/auth/login

# English
curl -H "Accept-Language: en" http://localhost:8080/api/auth/login
```

### 2. Đổi ngôn ngữ qua Query Parameter
```bash
# Vietnamese
curl "http://localhost:8080/api/auth/login?lang=vi"

# English
curl "http://localhost:8080/api/auth/login?lang=en"
```

## Message Files

### Location
- `src/main/resources/i18n/messages_vi.properties` - Tiếng Việt
- `src/main/resources/i18n/messages_en.properties` - English

### Structure
```properties
# Authentication messages
auth.invalid.credentials=Email hoặc mật khẩu không đúng
auth.account.inactive=Tài khoản đã bị vô hiệu hóa
auth.rate.limit.exceeded=Quá nhiều lần đăng nhập thất bại. Vui lòng thử lại sau {0} phút.

# Validation messages
expense.name.required=Tên chi tiêu không được để trống
expense.amount.positive=Số tiền phải lớn hơn 0

# Error messages
error.not.found=Không tìm thấy tài nguyên
error.internal.server=Đã xảy ra lỗi hệ thống
```

## Sử dụng trong Code

### 1. Inject MessageUtil
```java
@Service
public class MyService {
    private final MessageUtil messageUtil;

    public MyService(MessageUtil messageUtil) {
        this.messageUtil = messageUtil;
    }
}
```

### 2. Lấy message đơn giản
```java
String message = messageUtil.getMessage("auth.invalid.credentials");
// Vi: "Email hoặc mật khẩu không đúng"
// En: "Invalid email or password"
```

### 3. Lấy message với tham số
```java
String message = messageUtil.getMessage("auth.rate.limit.exceeded", 15);
// Vi: "Quá nhiều lần đăng nhập thất bại. Vui lòng thử lại sau 15 phút."
// En: "Too many failed login attempts. Please try again after 15 minutes."
```

### 4. Lấy message với default value
```java
String message = messageUtil.getMessageOrDefault(
    "unknown.key",
    "Default message if key not found"
);
```

## Validation Messages

### Sử dụng trong DTO
```java
@Data
public class ExpenseRequest {
    @NotBlank(message = "{expense.name.required}")
    private String name;

    @Positive(message = "{expense.amount.positive}")
    private BigDecimal amount;
}
```

## Testing

### Test với Vietnamese
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -H "Accept-Language: vi" \
  -d '{"email":"wrong@example.com","password":"wrong"}'
```

Response:
```json
{
  "status": 400,
  "error": "Yêu cầu không hợp lệ",
  "message": "Email hoặc mật khẩu không đúng"
}
```

### Test với English
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -H "Accept-Language: en" \
  -d '{"email":"wrong@example.com","password":"wrong"}'
```

Response:
```json
{
  "status": 400,
  "error": "Bad request",
  "message": "Invalid email or password"
}
```

## Thêm Message Mới

### 1. Thêm vào messages_vi.properties
```properties
my.new.message=Tin nhắn mới của tôi với tham số {0}
```

### 2. Thêm vào messages_en.properties
```properties
my.new.message=My new message with parameter {0}
```

### 3. Sử dụng trong code
```java
String message = messageUtil.getMessage("my.new.message", "value");
```

## Best Practices

✅ **DO:**
- Luôn thêm cả 2 ngôn ngữ (vi và en)
- Sử dụng key có ý nghĩa (auth.invalid.credentials)
- Dùng tham số {0}, {1} cho dynamic values
- Test cả 2 ngôn ngữ

❌ **DON'T:**
- Hardcode message trong code
- Quên thêm một trong 2 ngôn ngữ
- Dùng key không rõ nghĩa (msg1, error123)
