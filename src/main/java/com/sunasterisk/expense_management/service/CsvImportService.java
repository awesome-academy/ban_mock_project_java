package com.sunasterisk.expense_management.service;

import com.sunasterisk.expense_management.dto.csv.CsvImportResult;
import com.sunasterisk.expense_management.entity.*;
import com.sunasterisk.expense_management.entity.Category.CategoryType;
import com.sunasterisk.expense_management.repository.*;
import com.sunasterisk.expense_management.util.MessageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for importing data from CSV format
 * Logic: If ID exists in CSV and entity exists in DB -> Update, otherwise -> Create
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CsvImportService {

    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;
    private final IncomeRepository incomeRepository;
    private final CategoryRepository categoryRepository;
    private final BudgetRepository budgetRepository;
    private final PasswordEncoder passwordEncoder;
    private final MessageUtil messageUtil;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String DEFAULT_PASSWORD = "Password123!"; // Default password for imported users

    /**
     * Import users from CSV file
     * CSV format: id,name,email,phone,role,active,total_expenses,total_incomes,balance,created_at
     * Logic: If ID exists -> update, otherwise -> create new
     */
    public CsvImportResult importUsers(MultipartFile file) throws Exception {
        validateFile(file);

        List<String> lines = readCsvFile(file);
        if (lines.isEmpty()) {
            throw new IllegalArgumentException(messageUtil.getMessage("csv.import.file.empty"));
        }

        CsvImportResult result = CsvImportResult.builder()
                .totalRows(lines.size() - 1) // Exclude header
                .build();

        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            try {
                String[] fields = parseCsvLine(line);
                if (fields.length < 6) {
                    result.addError(i + 1, "Insufficient fields (expected 6, got " + fields.length + ")", line);
                    continue;
                }

                String idStr = fields[0].trim();
                String name = fields[1].trim();
                String email = fields[2].trim();
                String phone = fields[3].trim();
                String roleStr = fields[4].trim();
                String activeStr = fields[5].trim();

                User user;

                // Check if update or create
                if (!idStr.isEmpty()) {
                    try {
                        Long id = Long.parseLong(idStr);
                        user = userRepository.findById(id).orElse(null);
                        if (user != null) {
                            // Update existing user
                            user.setName(name);
                            user.setEmail(email);
                            user.setPhone(phone.isEmpty() ? null : phone);
                            try {
                                user.setRole(User.Role.valueOf(roleStr));
                            } catch (IllegalArgumentException e) {
                                user.setRole(User.Role.USER);
                            }
                            user.setActive(Boolean.parseBoolean(activeStr));
                            userRepository.save(user);
                            result.setSuccessCount(result.getSuccessCount() + 1);
                            continue;
                        }
                    } catch (NumberFormatException e) {
                        result.addError(i + 1, "Invalid ID format: " + idStr, line);
                        continue;
                    }
                }

                // Create new user (skip if email exists)
                if (userRepository.existsByEmail(email)) {
                    result.addError(i + 1, "Email already exists: " + email, line);
                    continue;
                }

                user = new User();
                user.setName(name);
                user.setEmail(email);
                user.setPhone(phone.isEmpty() ? null : phone);
                user.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));

                try {
                    user.setRole(User.Role.valueOf(roleStr));
                } catch (IllegalArgumentException e) {
                    user.setRole(User.Role.USER);
                }

                user.setActive(Boolean.parseBoolean(activeStr));

                userRepository.save(user);
                result.setSuccessCount(result.getSuccessCount() + 1);

            } catch (Exception e) {
                result.addError(i + 1, e.getMessage(), line);
                log.error("Error importing user at line {}: {}", i + 1, e.getMessage(), e);
            }
        }

        log.info("Imported {} users from CSV ({} success, {} errors)",
                lines.size() - 1, result.getSuccessCount(), result.getErrorCount());
        return result;
    }

    /**
     * Import expenses from CSV file
     * CSV format: id,name,amount,date,category,note,user,created_at
     * Logic: If ID exists -> update, otherwise -> create new
     */
    public CsvImportResult importExpenses(MultipartFile file) throws Exception {
        validateFile(file);

        List<String> lines = readCsvFile(file);
        if (lines.isEmpty()) {
            throw new IllegalArgumentException(messageUtil.getMessage("csv.import.file.empty"));
        }

        CsvImportResult result = CsvImportResult.builder()
                .totalRows(lines.size() - 1)
                .build();

        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            try {
                String[] fields = parseCsvLine(line);
                if (fields.length < 7) {
                    result.addError(i + 1, "Insufficient fields (expected 7, got " + fields.length + ")", line);
                    continue;
                }

                String idStr = fields[0].trim();
                String name = fields[1].trim();
                BigDecimal amount = new BigDecimal(fields[2].trim());
                LocalDate date = LocalDate.parse(fields[3].trim(), DATE_FORMATTER);
                String categoryName = fields[4].trim();
                String note = fields[5].trim();
                String userName = fields[6].trim();

                // Find category by name
                Category category = categoryRepository.findByNameAndType(categoryName, CategoryType.EXPENSE)
                        .orElse(null);
                if (category == null) {
                    result.addError(i + 1, "Category not found: " + categoryName, line);
                    continue;
                }

                // Find user by name
                User user = userRepository.findByName(userName).orElse(null);
                if (user == null) {
                    result.addError(i + 1, "User not found: " + userName, line);
                    continue;
                }

                Expense expense;

                // Check if update or create
                if (!idStr.isEmpty()) {
                    try {
                        Long id = Long.parseLong(idStr);
                        expense = expenseRepository.findById(id).orElse(null);
                        if (expense != null) {
                            // Update existing expense
                            expense.setName(name);
                            expense.setAmount(amount);
                            expense.setExpenseDate(date);
                            expense.setCategory(category);
                            expense.setNote(note.isEmpty() ? null : note);
                            expense.setUser(user);
                            expenseRepository.save(expense);
                            result.setSuccessCount(result.getSuccessCount() + 1);
                            continue;
                        }
                    } catch (NumberFormatException e) {
                        result.addError(i + 1, "Invalid ID format: " + idStr, line);
                        continue;
                    }
                }

                // Create new expense
                expense = new Expense();
                expense.setName(name);
                expense.setAmount(amount);
                expense.setExpenseDate(date);
                expense.setCategory(category);
                expense.setNote(note.isEmpty() ? null : note);
                expense.setUser(user);
                expense.setPaymentMethod(Expense.PaymentMethod.CASH);
                expense.setIsRecurring(false);

                expenseRepository.save(expense);
                result.setSuccessCount(result.getSuccessCount() + 1);

            } catch (Exception e) {
                result.addError(i + 1, e.getMessage(), line);
                log.error("Error importing expense at line {}: {}", i + 1, e.getMessage(), e);
            }
        }

        log.info("Imported {} expenses from CSV ({} success, {} errors)",
                lines.size() - 1, result.getSuccessCount(), result.getErrorCount());
        return result;
    }

    /**
     * Import incomes from CSV file
     * CSV format: id,name,amount,date,category,note,user,created_at
     * Logic: If ID exists -> update, otherwise -> create new
     */
    public CsvImportResult importIncomes(MultipartFile file) throws Exception {
        validateFile(file);

        List<String> lines = readCsvFile(file);
        if (lines.isEmpty()) {
            throw new IllegalArgumentException(messageUtil.getMessage("csv.import.file.empty"));
        }

        CsvImportResult result = CsvImportResult.builder()
                .totalRows(lines.size() - 1)
                .build();

        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            try {
                String[] fields = parseCsvLine(line);
                if (fields.length < 7) {
                    result.addError(i + 1, "Insufficient fields (expected 7, got " + fields.length + ")", line);
                    continue;
                }

                String idStr = fields[0].trim();
                String name = fields[1].trim();
                BigDecimal amount = new BigDecimal(fields[2].trim());
                LocalDate date = LocalDate.parse(fields[3].trim(), DATE_FORMATTER);
                String categoryName = fields[4].trim();
                String note = fields[5].trim();
                String userName = fields[6].trim();

                // Find category by name
                Category category = categoryRepository.findByNameAndType(categoryName, CategoryType.INCOME)
                        .orElse(null);
                if (category == null) {
                    result.addError(i + 1, "Category not found: " + categoryName, line);
                    continue;
                }

                // Find user by name
                User user = userRepository.findByName(userName).orElse(null);
                if (user == null) {
                    result.addError(i + 1, "User not found: " + userName, line);
                    continue;
                }

                Income income;

                // Check if update or create
                if (!idStr.isEmpty()) {
                    try {
                        Long id = Long.parseLong(idStr);
                        income = incomeRepository.findById(id).orElse(null);
                        if (income != null) {
                            // Update existing income
                            income.setName(name);
                            income.setAmount(amount);
                            income.setIncomeDate(date);
                            income.setCategory(category);
                            income.setNote(note.isEmpty() ? null : note);
                            income.setUser(user);
                            incomeRepository.save(income);
                            result.setSuccessCount(result.getSuccessCount() + 1);
                            continue;
                        }
                    } catch (NumberFormatException e) {
                        result.addError(i + 1, "Invalid ID format: " + idStr, line);
                        continue;
                    }
                }

                // Create new income
                income = new Income();
                income.setName(name);
                income.setAmount(amount);
                income.setIncomeDate(date);
                income.setCategory(category);
                income.setNote(note.isEmpty() ? null : note);
                income.setUser(user);
                income.setIsRecurring(false);

                incomeRepository.save(income);
                result.setSuccessCount(result.getSuccessCount() + 1);
            } catch (Exception e) {
                result.addError(i + 1, e.getMessage(), line);
                log.error("Error importing income at line {}: {}", i + 1, e.getMessage(), e);
            }
        }

        log.info("Imported {} incomes from CSV ({} success, {} errors)",
                lines.size() - 1, result.getSuccessCount(), result.getErrorCount());
        return result;
    }

    /**
     * Import categories from CSV file
     * CSV format: id,name,description,type,icon,created_at
     * Logic: If ID exists -> update, otherwise -> create new
     */
    public CsvImportResult importCategories(MultipartFile file) {
        CsvImportResult result = CsvImportResult.builder()
                .totalRows(0)
                .build();

        try {
            // Validate file
            if (file == null || file.isEmpty()) {
                result.addError(0, messageUtil.getMessage("csv.import.file.required"), "");
                return result;
            }

            String filename = file.getOriginalFilename();
            if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
                result.addError(0, messageUtil.getMessage("csv.import.file.invalid.format"), "");
                return result;
            }

            List<String> lines = readCsvFile(file);
            if (lines.isEmpty()) {
                result.addError(0, messageUtil.getMessage("csv.import.file.empty"), "");
                return result;
            }

            result.setTotalRows(lines.size() - 1);

            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);
                try {
                    String[] fields = parseCsvLine(line);
                    if (fields.length < 5) {
                        result.addError(i + 1, "Insufficient fields (expected 5, got " + fields.length + ")", line);
                        continue;
                    }

                    String idStr = fields[0].trim();
                    String name = fields[1].trim();
                    String description = fields[2].trim();
                    String typeStr = fields[3].trim();
                    String icon = fields[4].trim();

                    CategoryType type = CategoryType.valueOf(typeStr);
                    Category category;

                    // Check if update or create
                    if (!idStr.isEmpty()) {
                        try {
                            Long id = Long.parseLong(idStr);
                            category = categoryRepository.findById(id).orElse(null);
                            if (category != null) {
                                // Update existing category
                                category.setName(name);
                                category.setDescription(description.isEmpty() ? null : description);
                                category.setType(type);
                                category.setIcon(icon.isEmpty() ? null : icon);
                                try {
                                    categoryRepository.save(category);
                                    result.setSuccessCount(result.getSuccessCount() + 1);
                                } catch (Exception saveEx) {
                                    result.addError(i + 1, saveEx.getMessage(), line);
                                }
                                continue;
                            }
                        } catch (NumberFormatException e) {
                            result.addError(i + 1, "Invalid ID format: " + idStr, line);
                            continue;
                        }
                    }

                    // Create new category (skip if name+type exists)
                    if (categoryRepository.findByNameAndType(name, type).isPresent()) {
                        result.addError(i + 1, "Category already exists: " + name + " (" + type + ")", line);
                        continue;
                    }

                    category = new Category();
                    category.setName(name);
                    category.setDescription(description.isEmpty() ? null : description);
                    category.setType(type);
                    category.setIcon(icon.isEmpty() ? null : icon);
                    category.setActive(true);
                    category.setIsDefault(false);

                    try {
                        categoryRepository.save(category);
                        result.setSuccessCount(result.getSuccessCount() + 1);
                    } catch (Exception saveEx) {
                        result.addError(i + 1, saveEx.getMessage(), line);
                    }

                } catch (Exception e) {
                    result.addError(i + 1, e.getMessage(), line);
                    log.error("Error importing category at line {}: {}", i + 1, e.getMessage(), e);
                }
            }

            log.info("Imported {} categories from CSV ({} success, {} errors)",
                    lines.size() - 1, result.getSuccessCount(), result.getErrorCount());
            return result;

        } catch (Throwable e) {
            log.error("Error reading CSV file: {}", e.getMessage(), e);
            result.addError(0, "File reading error: " + e.getMessage(), "");
            return result;
        }
    }

    /**
     * Import budgets from CSV file
     * CSV format: id,name,amount,spent,remaining,month,category,user,created_at
     * Logic: If ID exists -> update, otherwise -> create new
     */
    public CsvImportResult importBudgets(MultipartFile file) throws Exception {
        validateFile(file);

        List<String> lines = readCsvFile(file);
        if (lines.isEmpty()) {
            throw new IllegalArgumentException(messageUtil.getMessage("csv.import.file.empty"));
        }

        CsvImportResult result = CsvImportResult.builder()
                .totalRows(lines.size() - 1)
                .build();

        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            try {
                String[] fields = parseCsvLine(line);
                if (fields.length < 8) {
                    result.addError(i + 1, "Insufficient fields (expected 8, got " + fields.length + ")", line);
                    continue;
                }

                String idStr = fields[0].trim();
                String name = fields[1].trim();
                BigDecimal amount = new BigDecimal(fields[2].trim());
                String monthStr = fields[5].trim(); // Format: YYYY-MM
                String categoryName = fields[6].trim();
                String userName = fields[7].trim();

                // Parse month (YYYY-MM)
                String[] monthParts = monthStr.split("-");
                int year = Integer.parseInt(monthParts[0]);
                int month = Integer.parseInt(monthParts[1]);

                // Find category
                Category category = categoryRepository.findByName(categoryName).orElse(null);
                if (category == null) {
                    result.addError(i + 1, "Category not found: " + categoryName, line);
                    continue;
                }

                // Find user
                User user = userRepository.findByName(userName).orElse(null);
                if (user == null) {
                    result.addError(i + 1, "User not found: " + userName, line);
                    continue;
                }

                Budget budget;

                // Check if update or create
                if (!idStr.isEmpty()) {
                    try {
                        Long id = Long.parseLong(idStr);
                        budget = budgetRepository.findById(id).orElse(null);
                        if (budget != null) {
                            // Update existing budget
                            budget.setName(name);
                            budget.setAmountLimit(amount);
                            budget.setMonth(month);
                            budget.setYear(year);
                            budget.setCategory(category);
                            budget.setUser(user);
                            budgetRepository.save(budget);
                            result.setSuccessCount(result.getSuccessCount() + 1);
                            continue;
                        }
                    } catch (NumberFormatException e) {
                        result.addError(i + 1, "Invalid ID format: " + idStr, line);
                        continue;
                    }
                }

                // Create new budget
                budget = new Budget();
                budget.setName(name);
                budget.setAmountLimit(amount);
                budget.setMonth(month);
                budget.setYear(year);
                budget.setCategory(category);
                budget.setUser(user);
                budget.setSpentAmount(BigDecimal.ZERO);

                budgetRepository.save(budget);
                result.setSuccessCount(result.getSuccessCount() + 1);
            } catch (Exception e) {
                result.addError(i + 1, e.getMessage(), line);
                log.error("Error importing budget at line {}: {}", i + 1, e.getMessage(), e);
            }
        }

        log.info("Imported {} budgets from CSV ({} success, {} errors)",
                lines.size() - 1, result.getSuccessCount(), result.getErrorCount());
        return result;
    }

    /**
     * Validate uploaded file
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(messageUtil.getMessage("csv.import.file.required"));
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
            throw new IllegalArgumentException(messageUtil.getMessage("csv.import.file.invalid.format"));
        }
    }

    /**
     * Read CSV file and return list of lines
     */
    private List<String> readCsvFile(MultipartFile file) throws Exception {
        List<String> lines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            // Skip BOM if present
            reader.mark(1);
            int firstChar = reader.read();
            if (firstChar != 0xFEFF) {
                reader.reset();
            }

            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    lines.add(line);
                }
            }
        }

        return lines;
    }

    /**
     * Parse CSV line handling quoted fields
     */
    private String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    // Double quote - add single quote to field
                    currentField.append('"');
                    i++; // Skip next quote
                } else {
                    // Toggle quote state
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                // Field separator
                fields.add(currentField.toString());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }

        // Add last field
        fields.add(currentField.toString());

        return fields.toArray(new String[0]);
    }
}
