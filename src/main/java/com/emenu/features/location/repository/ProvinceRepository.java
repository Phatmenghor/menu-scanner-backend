package com.emenu.features.location.repository;

import com.emenu.features.location.models.Province;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProvinceRepository extends JpaRepository<Province, UUID> {

    /**
     * Finds a non-deleted province by ID
     */
    Optional<Province> findByIdAndIsDeletedFalse(UUID id);

    /**
     * Finds a non-deleted province by province code
     */
    Optional<Province> findByProvinceCodeAndIsDeletedFalse(String provinceCode);

    /**
     * Checks if a non-deleted province exists with the given province code
     */
    boolean existsByProvinceCodeAndIsDeletedFalse(String provinceCode);

    /**
     * Finds a non-deleted province by English name
     */
    Optional<Province> findByProvinceEnAndIsDeletedFalse(String nameEn);

    /**
     * Finds a non-deleted province by Khmer name
     */
    Optional<Province> findByProvinceKhAndIsDeletedFalse(String nameKh);

    /**
     * Finds all non-deleted provinces
     */
    List<Province> findAllByIsDeletedFalse();

    /**
     * Searches provinces with text search across code and names in both English and Khmer
     */
    @Query("SELECT p FROM Province p WHERE p.isDeleted = false " +
           "AND (:search IS NULL OR :search = '' OR " +
           "LOWER(p.provinceCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.provinceEn) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.provinceKh) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Province> searchProvinces(@Param("search") String search, Pageable pageable);
}