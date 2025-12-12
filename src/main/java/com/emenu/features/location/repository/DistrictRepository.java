package com.emenu.features.location.repository;

import com.emenu.features.location.models.District;
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
public interface DistrictRepository extends JpaRepository<District, UUID> {
    @Query("SELECT d FROM District d LEFT JOIN FETCH d.province WHERE d.id = :id AND d.isDeleted = false")
    Optional<District> findByIdAndIsDeletedFalse(@Param("id") UUID id);
    
    @Query("SELECT d FROM District d LEFT JOIN FETCH d.province WHERE d.districtCode = :code AND d.isDeleted = false")
    Optional<District> findByDistrictCodeAndIsDeletedFalse(@Param("code") String districtCode);
    
    @Query("SELECT d FROM District d LEFT JOIN FETCH d.province WHERE d.districtEn = :nameEn AND d.isDeleted = false")
    Optional<District> findByDistrictEnAndIsDeletedFalse(@Param("nameEn") String districtEn);
    
    @Query("SELECT d FROM District d LEFT JOIN FETCH d.province WHERE d.districtKh = :nameKh AND d.isDeleted = false")
    Optional<District> findByDistrictKhAndIsDeletedFalse(@Param("nameKh") String districtKh);
    
    boolean existsByDistrictCodeAndIsDeletedFalse(String districtCode);
    
    @Query("SELECT d FROM District d LEFT JOIN FETCH d.province WHERE d.provinceCode = :provinceCode AND d.isDeleted = false")
    List<District> findAllByProvinceCodeAndIsDeletedFalse(@Param("provinceCode") String provinceCode);
    
    @Query("SELECT d FROM District d LEFT JOIN FETCH d.province " +
           "WHERE d.isDeleted = false " +
           "AND (:provinceCode IS NULL OR :provinceCode = '' OR d.provinceCode = :provinceCode) " +
           "AND (:search IS NULL OR :search = '' OR " +
           "LOWER(d.districtCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(d.districtEn) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(d.districtKh) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<District> searchDistricts(@Param("provinceCode") String provinceCode,
                                   @Param("search") String search, Pageable pageable);
}