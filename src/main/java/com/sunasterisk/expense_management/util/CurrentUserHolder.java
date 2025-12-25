package com.sunasterisk.expense_management.util;

import com.sunasterisk.expense_management.entity.User;
import com.sunasterisk.expense_management.exception.ResourceNotFoundException;
import com.sunasterisk.expense_management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

/**
 * Request-scoped bean that caches the current authenticated user
 * to avoid redundant database queries within the same request.
 */
@Component
@RequestScope
@RequiredArgsConstructor
public class CurrentUserHolder {

    private final UserRepository userRepository;
    private final MessageUtil messageUtil;
    private User cachedUser;

    /**
     * Get the current authenticated user, cached for the request lifetime.
     *
     * @return the current authenticated user
     * @throws ResourceNotFoundException if user not found
     */
    public User getCurrentUser() {
        if (cachedUser == null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()
                    || "anonymousUser".equals(authentication.getPrincipal())) {
                return null;
            }
            String email = authentication.getName();
            cachedUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            messageUtil.getMessage("user.not.found")));
        }
        return cachedUser;
    }

    /**
     * Get the current authenticated user, ensuring it exists.
     *
     * @return the current authenticated user
     * @throws ResourceNotFoundException if user not found or not authenticated
     */
    public User getCurrentUserOrThrow() {
        User user = getCurrentUser();
        if (user == null) {
            throw new ResourceNotFoundException(messageUtil.getMessage("user.not.found"));
        }
        return user;
    }
}
