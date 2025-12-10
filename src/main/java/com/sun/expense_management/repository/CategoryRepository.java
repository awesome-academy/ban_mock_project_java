package com.sun.expense_management.repository;

import com.sun.expense_management.entity.Category;
import com.sun.expense_management.entity.Category.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long>, JpaSpecificationExecutor<Category> {

    List<Category> findByTypeAndActiveTrue(CategoryType type);

    Optional<Category> findByIdAndActiveTrue(Long id);

    List<Category> findByActiveTrue();

    Optional<Category> findByIdAndUser_Id(Long id, Long userId);

    Optional<Category> findByIdAndUser_IdAndActiveTrue(Long id, Long userId);
}
