# MapStruct Integration

## Tổng quan

Dự án đã tích hợp **MapStruct 1.6.3** để tự động generate code mapping giữa Entity và DTO, thay thế các phương thức mapping thủ công.

## Cấu hình

### Maven Dependencies (`pom.xml`)

```xml
<properties>
    <org.mapstruct.version>1.6.3</org.mapstruct.version>
    <lombok-mapstruct-binding.version>0.2.0</lombok-mapstruct-binding.version>
</properties>

<dependencies>
    <!-- MapStruct -->
    <dependency>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct</artifactId>
        <version>${org.mapstruct.version}</version>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <configuration>
                <annotationProcessorPaths>
                    <path>
                        <groupId>org.mapstruct</groupId>
                        <artifactId>mapstruct-processor</artifactId>
                        <version>${org.mapstruct.version}</version>
                    </path>
                    <path>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok</artifactId>
                        <version>${lombok.version}</version>
                    </path>
                    <path>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok-mapstruct-binding</artifactId>
                        <version>${lombok-mapstruct-binding.version}</version>
                    </path>
                </annotationProcessorPaths>
            </configuration>
        </plugin>
    </plugins>
</build>
```

**Lưu ý:** Thứ tự trong `annotationProcessorPaths` rất quan trọng:
1. MapStruct processor phải đứng trước
2. Lombok
3. Lombok-MapStruct binding (để tích hợp với Lombok)

---

## Mappers đã implement

### 1. IncomeMapper

**Interface:** `/mapper/IncomeMapper.java`

```java
@Mapper(componentModel = "spring")
public interface IncomeMapper {

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "categoryIcon", source = "category.icon")
    IncomeResponse toResponse(Income income);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Income toEntity(IncomeRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(IncomeRequest request, @MappingTarget Income income);
}
```

**Sử dụng trong IncomeService:**

```java
@Service
public class IncomeService {
    private final IncomeMapper incomeMapper;

    // Trước khi dùng MapStruct (Manual mapping):
    return IncomeResponse.fromEntity(income);

    // Sau khi dùng MapStruct:
    return incomeMapper.toResponse(income);
}
```

### 2. ExpenseMapper

**Interface:** `/mapper/ExpenseMapper.java`

```java
@Mapper(componentModel = "spring")
public interface ExpenseMapper {

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "categoryIcon", source = "category.icon")
    ExpenseResponse toResponse(Expense expense);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "attachments", ignore = true)
    Expense toEntity(ExpenseRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "attachments", ignore = true)
    void updateEntity(ExpenseRequest request, @MappingTarget Expense expense);
}
```

---

## Tính năng chính

### 1. Entity → DTO (toResponse)

```java
// MapStruct tự động map tất cả các field
Income income = ...;
IncomeResponse response = incomeMapper.toResponse(income);
```

**Nested mapping tự động:**
```java
@Mapping(target = "categoryId", source = "category.id")
@Mapping(target = "categoryName", source = "category.name")
@Mapping(target = "categoryIcon", source = "category.icon")
```

MapStruct tự động:
- Check null cho `category`
- Extract `category.id`, `category.name`, `category.icon`
- Map vào response DTO

### 2. DTO → Entity (toEntity)

```java
IncomeRequest request = ...;
Income income = incomeMapper.toEntity(request);

// Sau đó set manually các field không map:
income.setUser(user);
income.setCategory(category);
```

**Ignore các field không cần map:**
```java
@Mapping(target = "id", ignore = true)           // Auto-generated
@Mapping(target = "user", ignore = true)         // Set manually
@Mapping(target = "category", ignore = true)     // Set manually
@Mapping(target = "createdAt", ignore = true)    // Auto-set by @PrePersist
@Mapping(target = "updatedAt", ignore = true)    // Auto-set by @PrePersist
```

### 3. Update Entity (updateEntity)

```java
IncomeRequest request = ...;
Income existingIncome = ...;

// Update tất cả fields từ request vào existing entity
incomeMapper.updateEntity(request, existingIncome);

// Sau đó update category nếu cần
existingIncome.setCategory(newCategory);
```

**Ưu điểm:** Không cần viết từng dòng `setXxx()`

---

## So sánh Before/After

### Trước khi dùng MapStruct

**ExpenseResponse.java:**
```java
public static ExpenseResponse fromEntity(Expense expense) {
    return ExpenseResponse.builder()
        .id(expense.getId())
        .name(expense.getName())
        .amount(expense.getAmount())
        .expenseDate(expense.getExpenseDate())
        .note(expense.getNote())
        .location(expense.getLocation())
        .paymentMethod(expense.getPaymentMethod())
        .isRecurring(expense.getIsRecurring())
        .recurringType(expense.getRecurringType())
        .createdAt(expense.getCreatedAt())
        .updatedAt(expense.getUpdatedAt())
        .categoryId(expense.getCategory() != null ? expense.getCategory().getId() : null)
        .categoryName(expense.getCategory() != null ? expense.getCategory().getName() : null)
        .categoryIcon(expense.getCategory() != null ? expense.getCategory().getIcon() : null)
        .build();
}
```
→ **46 dòng code thủ công**

**ExpenseService.java:**
```java
Expense expense = Expense.builder()
    .name(request.getName())
    .amount(request.getAmount())
    .expenseDate(request.getExpenseDate())
    .note(request.getNote())
    .location(request.getLocation())
    .paymentMethod(request.getPaymentMethod())
    .isRecurring(request.getIsRecurring())
    .recurringType(request.getRecurringType())
    .user(user)
    .category(category)
    .build();
```
→ **13 dòng code builder pattern**

### Sau khi dùng MapStruct

**ExpenseMapper.java:**
```java
@Mapper(componentModel = "spring")
public interface ExpenseMapper {
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "categoryIcon", source = "category.icon")
    ExpenseResponse toResponse(Expense expense);

    // ... other methods
}
```
→ **Chỉ khai báo interface**

**ExpenseService.java:**
```java
Expense expense = expenseMapper.toEntity(request);
expense.setUser(user);
expense.setCategory(category);
```
→ **3 dòng code**

---

## Generated Code

MapStruct tự động generate implementation tại compile-time:

**Location:** `target/generated-sources/annotations/com/sun/expense_management/mapper/`

- `IncomeMapperImpl.java`
- `ExpenseMapperImpl.java`

**Ví dụ generated code:**

```java
@Component
public class IncomeMapperImpl implements IncomeMapper {

    @Override
    public IncomeResponse toResponse(Income income) {
        if (income == null) {
            return null;
        }

        IncomeResponse.IncomeResponseBuilder incomeResponse = IncomeResponse.builder();

        incomeResponse.categoryId(incomeCategoryId(income));
        incomeResponse.categoryName(incomeCategoryName(income));
        incomeResponse.categoryIcon(incomeCategoryIcon(income));
        incomeResponse.id(income.getId());
        incomeResponse.name(income.getName());
        // ... all fields

        return incomeResponse.build();
    }

    private Long incomeCategoryId(Income income) {
        Category category = income.getCategory();
        if (category == null) {
            return null;
        }
        return category.getId();
    }
    // ... helper methods
}
```

---

## Ưu điểm

✅ **Ít code hơn:** Chỉ cần khai báo interface, không cần implement
✅ **Type-safe:** Compile-time checking, phát hiện lỗi sớm
✅ **Performance cao:** Generate Java code thuần, không dùng reflection
✅ **Dễ maintain:** Thêm field mới → MapStruct tự động map
✅ **Null-safe:** Tự động check null cho nested objects
✅ **Tích hợp tốt với Lombok:** `@Builder`, `@Data`, etc.
✅ **Spring Integration:** `componentModel = "spring"` → Tự động inject

---

## Nhược điểm

❌ **Learning curve:** Phải học cú pháp annotation
❌ **Compile-time required:** Phải compile để generate code
❌ **IDE support:** Cần plugin để jump to generated code

---

## Best Practices

### 1. Đặt tên method rõ ràng

```java
// ✅ Good
IncomeResponse toResponse(Income income);
Income toEntity(IncomeRequest request);
void updateEntity(IncomeRequest request, @MappingTarget Income income);

// ❌ Bad
IncomeResponse map(Income income);
Income convert(IncomeRequest request);
```

### 2. Ignore các field không cần map

```java
@Mapping(target = "id", ignore = true)
@Mapping(target = "createdAt", ignore = true)
@Mapping(target = "updatedAt", ignore = true)
```

### 3. Sử dụng source cho nested mapping

```java
@Mapping(target = "categoryId", source = "category.id")
@Mapping(target = "categoryName", source = "category.name")
```

### 4. Tận dụng `@MappingTarget` cho update

```java
void updateEntity(IncomeRequest request, @MappingTarget Income income);
```

---

## Troubleshooting

### 1. MapStruct không generate code

**Nguyên nhân:** Thứ tự annotation processor không đúng

**Giải pháp:**
```xml
<annotationProcessorPaths>
    <!-- MapStruct phải đứng TRƯỚC Lombok -->
    <path>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct-processor</artifactId>
    </path>
    <path>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
    </path>
    <path>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok-mapstruct-binding</artifactId>
    </path>
</annotationProcessorPaths>
```

### 2. IDE không nhận diện mapper

**Giải pháp:**
- IntelliJ: Install "MapStruct Support" plugin
- Eclipse: Enable "Annotation Processing" in settings

### 3. Compile lỗi "cannot find symbol"

**Nguyên nhân:** Generated code chưa được generate

**Giải pháp:**
```bash
./mvnw clean compile
```

---

## Tài liệu tham khảo

- [MapStruct Official Documentation](https://mapstruct.org/)
- [MapStruct Reference Guide](https://mapstruct.org/documentation/stable/reference/html/)
- [MapStruct + Lombok](https://mapstruct.org/faq/#can-i-use-mapstruct-together-with-project-lombok)

---

## Kết luận

MapStruct đã được tích hợp thành công vào project với:
- ✅ 2 Mappers: `IncomeMapper`, `ExpenseMapper`
- ✅ Loại bỏ ~100 dòng code mapping thủ công
- ✅ Type-safe compilation
- ✅ Null-safe nested mapping
- ✅ Easy to maintain and extend

Recommendation của CTO về sử dụng MapStruct là hợp lý và đã được áp dụng!
