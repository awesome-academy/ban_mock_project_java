package com.sun.expense_management.service;

import com.sun.expense_management.dto.AuthRequest;
import com.sun.expense_management.dto.AuthResponse;
import com.sun.expense_management.entity.User;
import com.sun.expense_management.exception.RateLimitExceededException;
import com.sun.expense_management.repository.UserRepository;
import com.sun.expense_management.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final LoginRateLimiterService rateLimiterService;
    private final HttpServletRequest request;

    public AuthService(UserRepository userRepository,
                      PasswordEncoder passwordEncoder,
                      JwtUtil jwtUtil,
                      LoginRateLimiterService rateLimiterService,
                      HttpServletRequest request) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.rateLimiterService = rateLimiterService;
        this.request = request;
    }

    public AuthResponse login(AuthRequest authRequest) {
        String ipAddress = getClientIP();

        // Check rate limit before processing login
        if (!rateLimiterService.allowLogin(ipAddress)) {
            long remaining = rateLimiterService.getRemainingAttempts(ipAddress);
            log.warn("Login rate limit exceeded for IP: {} (remaining: {})", ipAddress, remaining);

            throw new RateLimitExceededException(
                String.format("Quá nhiều lần đăng nhập thất bại. Vui lòng thử lại sau %d phút.",
                    rateLimiterService.getLockoutDurationMinutes()),
                rateLimiterService.getLockoutDurationMinutes()
            );
        }

        User user = userRepository.findByEmail(authRequest.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login failed - user not found: {} from IP: {}", authRequest.getEmail(), ipAddress);
                    return new IllegalArgumentException("Email hoặc mật khẩu không đúng");
                });

        if (!passwordEncoder.matches(authRequest.getPassword(), user.getPassword())) {
            log.warn("Login failed - invalid password for: {} from IP: {}", authRequest.getEmail(), ipAddress);
            throw new IllegalArgumentException("Email hoặc mật khẩu không đúng");
        }

        if (!user.getActive()) {
            log.warn("Login failed - inactive account: {} from IP: {}", authRequest.getEmail(), ipAddress);
            throw new IllegalArgumentException("Tài khoản đã bị vô hiệu hóa");
        }

        // Successful login - reset rate limit for this IP
        rateLimiterService.resetLimit(ipAddress);

        String token = jwtUtil.generateToken(user.getEmail());
        log.info("Login successful for: {} from IP: {}", user.getEmail(), ipAddress);

        return new AuthResponse(token);
    }

    /**
     * Get client IP address from request
     * Handles X-Forwarded-For header for proxied requests
     */
    private String getClientIP() {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }
}
