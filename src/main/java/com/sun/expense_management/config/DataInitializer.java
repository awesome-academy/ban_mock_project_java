package com.sun.expense_management.config;

import com.sun.expense_management.entity.Category;
import com.sun.expense_management.entity.Category.CategoryType;
import com.sun.expense_management.entity.User;
import com.sun.expense_management.repository.CategoryRepository;
import com.sun.expense_management.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    /**
     * Seed test users - ONLY for development profile
     * DO NOT use in production!
     */
    @Bean
    @Profile("dev")
    public CommandLineRunner seedUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByEmail("user@example.com").isEmpty()) {
                User u = User.builder()
                        .name("Demo User")
                        .email("user@example.com")
                        .password(passwordEncoder.encode("password"))
                        .role(User.Role.USER)
                        .active(true)
                        .build();

                userRepository.save(u);
                System.out.println("✅ Test user created: user@example.com / password");
            }

            if (userRepository.findByEmail("admin@example.com").isEmpty()) {
                User a = User.builder()
                        .name("Admin")
                        .email("admin@example.com")
                        .password(passwordEncoder.encode("adminpass"))
                        .role(User.Role.ADMIN)
                        .active(true)
                        .build();

                userRepository.save(a);
                System.out.println("✅ Test admin created: admin@example.com / adminpass");
            }
        };
    }

    @Bean
    public CommandLineRunner seedCategories(CategoryRepository categoryRepository) {
        return args -> {
            if (categoryRepository.count() == 0) {
                // Expense categories
                categoryRepository.save(Category.builder()
                        .name("Ăn uống")
                        .description("Chi tiêu cho ăn uống, nhà hàng, cafe")
                        .icon("🍔")
                        .color("#FF6B6B")
                        .type(CategoryType.EXPENSE)
                        .isDefault(true)
                        .active(true)
                        .build());

                categoryRepository.save(Category.builder()
                        .name("Di chuyển")
                        .description("Chi tiêu cho xăng xe, taxi, grab")
                        .icon("🚗")
                        .color("#4ECDC4")
                        .type(CategoryType.EXPENSE)
                        .isDefault(true)
                        .active(true)
                        .build());

                categoryRepository.save(Category.builder()
                        .name("Mua sắm")
                        .description("Chi tiêu cho quần áo, đồ dùng")
                        .icon("🛒")
                        .color("#45B7D1")
                        .type(CategoryType.EXPENSE)
                        .isDefault(true)
                        .active(true)
                        .build());

                categoryRepository.save(Category.builder()
                        .name("Giải trí")
                        .description("Chi tiêu cho phim, game, du lịch")
                        .icon("🎮")
                        .color("#96CEB4")
                        .type(CategoryType.EXPENSE)
                        .isDefault(true)
                        .active(true)
                        .build());

                categoryRepository.save(Category.builder()
                        .name("Hóa đơn & Tiện ích")
                        .description("Điện, nước, internet, điện thoại")
                        .icon("📱")
                        .color("#FFEAA7")
                        .type(CategoryType.EXPENSE)
                        .isDefault(true)
                        .active(true)
                        .build());

                categoryRepository.save(Category.builder()
                        .name("Sức khỏe")
                        .description("Khám bệnh, thuốc, gym")
                        .icon("💊")
                        .color("#DDA0DD")
                        .type(CategoryType.EXPENSE)
                        .isDefault(true)
                        .active(true)
                        .build());

                categoryRepository.save(Category.builder()
                        .name("Giáo dục")
                        .description("Học phí, sách vở, khóa học")
                        .icon("📚")
                        .color("#98D8C8")
                        .type(CategoryType.EXPENSE)
                        .isDefault(true)
                        .active(true)
                        .build());

                categoryRepository.save(Category.builder()
                        .name("Khác")
                        .description("Các khoản chi tiêu khác")
                        .icon("📦")
                        .color("#B8B8B8")
                        .type(CategoryType.EXPENSE)
                        .isDefault(true)
                        .active(true)
                        .build());

                // Income categories
                categoryRepository.save(Category.builder()
                        .name("Lương")
                        .description("Thu nhập từ lương hàng tháng")
                        .icon("💰")
                        .color("#2ECC71")
                        .type(CategoryType.INCOME)
                        .isDefault(true)
                        .active(true)
                        .build());

                categoryRepository.save(Category.builder()
                        .name("Thưởng")
                        .description("Tiền thưởng, bonus")
                        .icon("🎁")
                        .color("#F39C12")
                        .type(CategoryType.INCOME)
                        .isDefault(true)
                        .active(true)
                        .build());

                categoryRepository.save(Category.builder()
                        .name("Đầu tư")
                        .description("Lợi nhuận từ đầu tư, cổ phiếu")
                        .icon("📈")
                        .color("#3498DB")
                        .type(CategoryType.INCOME)
                        .isDefault(true)
                        .active(true)
                        .build());

                categoryRepository.save(Category.builder()
                        .name("Thu nhập khác")
                        .description("Các nguồn thu nhập khác")
                        .icon("💵")
                        .color("#9B59B6")
                        .type(CategoryType.INCOME)
                        .isDefault(true)
                        .active(true)
                        .build());

                System.out.println("✅ Default categories created successfully!");
            }
        };
    }
}
