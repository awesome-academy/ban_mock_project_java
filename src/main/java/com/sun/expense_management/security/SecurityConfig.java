package com.sun.expense_management.security;

import com.sun.expense_management.service.CustomUserDetailsService;
import com.sun.expense_management.util.MessageUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final AdminAccessDeniedHandler adminAccessDeniedHandler;
    private final MessageUtil messageUtil;

    public SecurityConfig(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService,
                          JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
                          AdminAccessDeniedHandler adminAccessDeniedHandler,
                          MessageUtil messageUtil) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.adminAccessDeniedHandler = adminAccessDeniedHandler;
        this.messageUtil = messageUtil;
    }

    /**
     * Security filter chain for static resources (WebJars, CSS, JS, etc.)
     * Order 1 - highest priority to bypass security completely
     */
    @Bean
    @Order(1)
    public SecurityFilterChain staticResourcesFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/webjars/**", "/css/**", "/js/**", "/fonts/**", "/webfonts/**", "/images/**", "/favicon.ico")
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .csrf(csrf -> csrf.disable());
        return http.build();
    }

    /**
     * Main security filter chain for application endpoints
     * Order 2 - applies after static resources
     */
    @Bean
    @Order(2)
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        JwtFilter jwtFilter = new JwtFilter(jwtUtil, userDetailsService, messageUtil);

        http
                // CSRF protection enabled for admin pages (form-based), disabled for API
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/**"))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(adminAccessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        // Admin endpoints - session-based authentication
                        .requestMatchers("/admin/login").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        // API endpoints - JWT authentication
                        .requestMatchers("/api/auth/**", "/actuator/**").permitAll()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().authenticated())
                // Session management: stateless for API, stateful for admin
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                // JWT filter only applies to /api/** paths
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                // Form login for admin
                .formLogin(form -> form
                        .loginPage("/admin/login")
                        .permitAll()
                        .disable()) // Disable default form login, we use custom controller
                .logout(logout -> logout
                        .logoutUrl("/admin/logout")
                        .logoutSuccessUrl("/admin/login?logout")
                        .permitAll()
                        .disable()); // Disable default logout, we use custom controller

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
