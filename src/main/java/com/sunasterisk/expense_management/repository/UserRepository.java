package com.sunasterisk.expense_management.repository;

import com.sunasterisk.expense_management.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);

    Optional<User> findByName(String name);

    boolean existsByEmail(String email);

    // Admin dashboard queries
    Long countByActive(Boolean active);
    Long countByCreatedAtAfter(LocalDateTime date);
}
