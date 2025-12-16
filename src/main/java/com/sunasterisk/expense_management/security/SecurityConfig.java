package com.sunasterisk.expense_management.security;

import com.sunasterisk.expense_management.service.CustomUserDetailsService;
import com.sunasterisk.expense_management.util.MessageUtil;
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
    private final MessageUtil messageUtil;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    public SecurityConfig(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService,
                          MessageUtil messageUtil, JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.messageUtil = messageUtil;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
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

    @Bean
    @Order(2)
    public SecurityFilterChain adminSecurityFilterChain(HttpSecurity http, AdminAccessDeniedHandler adminAccessDeniedHandler) throws Exception {
        http
            .securityMatcher("/admin/**")
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/admin/login").permitAll()
                .anyRequest().hasRole("ADMIN")
            )
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            )
            .exceptionHandling(exception ->
                exception.accessDeniedHandler(adminAccessDeniedHandler)
            );

        return http.build();
    }

    @Bean
    @Order(3)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http, AdminAccessDeniedHandler adminAccessDeniedHandler) throws Exception {
        JwtFilter jwtFilter = new JwtFilter(jwtUtil, userDetailsService, messageUtil);
        http
            .securityMatcher("/api/**")
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/actuator/**").permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .exceptionHandling(exception ->
                exception.authenticationEntryPoint(jwtAuthenticationEntryPoint)
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .csrf(csrf -> csrf.disable());

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
