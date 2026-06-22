package com.dawn.catalog.repository;

import com.dawn.catalog.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {
    List<Brand> findAllByDeletedAtIsNull();

    boolean existsByNameAndDeletedAtIsNull(String name);

    Optional<Brand> findByIdAndDeletedAtIsNull(Long id);
}
