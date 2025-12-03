package com.sun.expense_management.service;

import com.sun.expense_management.dto.AuthRequest;
import com.sun.expense_management.dto.AuthResponse;
import com.sun.expense_management.entity.User;
import com.sun.expense_management.repository.UserRepository;
import com.sun.expense_management.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(token);
    }
}
