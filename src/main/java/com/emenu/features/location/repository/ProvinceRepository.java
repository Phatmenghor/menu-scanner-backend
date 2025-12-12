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
    Optional<Province> findByIdAndIsDeletedFalse(UUID id);
    Optional<Province> findByProvinceCodeAndIsDeletedFalse(String provinceCode);
    Optional<Province> findByProvinceEnAndIsDeletedFalse(String provinceEn);
    Optional<Province> findByProvinceKhAndIsDeletedFalse(String provinceKh);
    boolean existsByProvinceCodeAndIsDeletedFalse(String provinceCode);
    List<Province> findAllByIsDeletedFalse();
    
    @Query("SELECT p FROM Province p WHERE p.isDeleted = false " +
           "AND (:search IS NULL OR :search = '' OR " +
           "LOWER(p.provinceCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.provinceEn) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.provinceKh) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Province> searchProvinces(@Param("search") String search, Pageable pageable);
}