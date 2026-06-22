package com.dawn.catalog.repository;

import com.dawn.catalog.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findAllByDeletedAtIsNull();

    boolean existsBySlugAndDeletedAtIsNull(String slug);

    Optional<Category> findByIdAndDeletedAtIsNull(Long id);

    boolean existsByParentIdAndDeletedAtIsNull(Long parentId);
}
