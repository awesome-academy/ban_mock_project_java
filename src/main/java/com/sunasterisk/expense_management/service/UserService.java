package com.sunasterisk.expense_management.service;

import com.sunasterisk.expense_management.dto.PageResponse;
import com.sunasterisk.expense_management.dto.UserDto;
import com.sunasterisk.expense_management.dto.user.AdminUserFilterRequest;
import com.sunasterisk.expense_management.entity.ActivityLog.ActionType;
import com.sunasterisk.expense_management.entity.User;
import com.sunasterisk.expense_management.exception.DuplicateResourceException;
import com.sunasterisk.expense_management.exception.ResourceNotFoundException;
import com.sunasterisk.expense_management.repository.UserRepository;
import com.sunasterisk.expense_management.repository.specification.UserSpecification;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for User management
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MessageSource messageSource;
    private final ActivityLogService activityLogService;
    private final ObjectMapper objectMapper;



    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email).orElse(null);
    }

    /**
     * Get all users
     */
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get all users with pagination
     */
    public Page<UserDto> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(UserDto::fromEntity);
    }

    /**
     * Get all users with filtering and pagination (for admin)
     */
    public PageResponse<UserDto> getAllUsers(AdminUserFilterRequest filter) {
        String[] sortFields = filter.getSortBy().split(",");
        Sort sort = filter.getSortDir().equalsIgnoreCase("asc")
                ? Sort.by(sortFields).ascending()
                : Sort.by(sortFields).descending();

        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);

        Specification<User> spec = UserSpecification.withFilters(
                filter.getName(),
                filter.getEmail(),
                filter.getRole(),
                filter.getActive()
        );

        Page<User> userPage = userRepository.findAll(spec, pageable);
        Page<UserDto> responsePage = userPage.map(UserDto::fromEntity);

        return PageResponse.fromPage(responsePage);
    }

    /**
     * Get user by ID
     */
    public UserDto getUserById(Long id) {
        return userRepository.findById(id)
                .map(UserDto::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageSource.getMessage("user.not.found", new Object[]{id}, LocaleContextHolder.getLocale())
                ));
    }

    /**
     * Create new user
     */
    @Transactional
    public UserDto createUser(UserDto dto) {
        // Check if email already exists
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new DuplicateResourceException(
                messageSource.getMessage("admin.user.email.exists", null, LocaleContextHolder.getLocale())
            );
        }

        User user = User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .phone(dto.getPhone())
                .role(dto.getRole() != null ? dto.getRole() : User.Role.USER)
                .active(dto.getActive() != null ? dto.getActive() : true)
                .build();

        User saved = userRepository.save(user);

        // Log activity with new values
        User currentUser = getCurrentUser();
        try {
            UserDto savedDto = UserDto.fromEntity(saved);
            savedDto.setPassword(null); // Don't log password
            String newValue = objectMapper.writeValueAsString(savedDto);
            activityLogService.logWithValues(
                ActionType.CREATE,
                currentUser,
                "User",
                saved.getId(),
                String.format("Created user '%s' (%s) with role %s", saved.getName(), saved.getEmail(), saved.getRole()),
                null,
                newValue
            );
        } catch (Exception e) {
            activityLogService.log(
                ActionType.CREATE,
                currentUser,
                "User",
                saved.getId(),
                String.format("Created user '%s' (%s) with role %s", saved.getName(), saved.getEmail(), saved.getRole())
            );
        }

        return UserDto.fromEntity(saved);
    }

    /**
     * Update user
     */
    @Transactional
    public UserDto updateUser(Long id, UserDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageSource.getMessage("user.not.found", new Object[]{id}, LocaleContextHolder.getLocale())
                ));

        // Check email uniqueness if changed
        if (!user.getEmail().equals(dto.getEmail())) {
            if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
                throw new DuplicateResourceException(
                        messageSource.getMessage("admin.user.email.exists", null, LocaleContextHolder.getLocale())
                );
            }
        }

        // Keep old value for logging
        String oldValue = null;
        try {
            UserDto oldDto = UserDto.fromEntity(user);
            oldDto.setPassword(null); // Don't log password
            oldValue = objectMapper.writeValueAsString(oldDto);
        } catch (Exception e) {
            // Ignore
        }

        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setRole(dto.getRole());
        user.setActive(dto.getActive());

        // Update password only if provided
        if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        User updated = userRepository.save(user);

        // Build description of changes
        StringBuilder changeDesc = new StringBuilder("Updated user: ");
        changeDesc.append(updated.getName()).append(" (").append(updated.getEmail()).append(")");

        // Log activity with old and new values
        User currentUser = getCurrentUser();
        try {
            UserDto updatedDto = UserDto.fromEntity(updated);
            updatedDto.setPassword(null); // Don't log password
            String newValue = objectMapper.writeValueAsString(updatedDto);
            activityLogService.logWithValues(
                ActionType.UPDATE,
                currentUser,
                "User",
                updated.getId(),
                changeDesc.toString(),
                oldValue,
                newValue
            );
        } catch (Exception e) {
            activityLogService.log(
                ActionType.UPDATE,
                currentUser,
                "User",
                updated.getId(),
                changeDesc.toString()
            );
        }

        return UserDto.fromEntity(updated);
    }

    /**
     * Delete user
     */
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageSource.getMessage("user.not.found", new Object[]{id}, LocaleContextHolder.getLocale())
                ));

        // Log activity before deletion with old values
        User currentUser = getCurrentUser();
        try {
            UserDto userDto = UserDto.fromEntity(user);
            userDto.setPassword(null); // Don't log password
            String oldValue = objectMapper.writeValueAsString(userDto);
            activityLogService.logWithValues(
                ActionType.DELETE,
                currentUser,
                "User",
                user.getId(),
                String.format("Deleted user '%s' (%s)", user.getName(), user.getEmail()),
                oldValue,
                null
            );
        } catch (Exception e) {
            activityLogService.log(
                ActionType.DELETE,
                currentUser,
                "User",
                user.getId(),
                String.format("Deleted user '%s' (%s)", user.getName(), user.getEmail())
            );
        }

        userRepository.delete(user);
    }

    /**
     * Check if user exists by email
     */
    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }
}
