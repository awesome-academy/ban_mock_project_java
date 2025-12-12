package com.sun.expense_management.repository;

import com.sun.expense_management.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    // Admin dashboard queries
    Long countByActive(Boolean active);
    Long countByCreatedAtAfter(LocalDateTime date);
}
