package com.emenu.features.location.repository;

import com.emenu.features.location.models.District;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DistrictRepository extends JpaRepository<District, UUID> {

    boolean existsByDistrictCodeAndIsDeletedFalse(String districtCode);

    @EntityGraph(attributePaths = {"province"})
    @Query("SELECT d FROM District d WHERE d.id = :id AND d.isDeleted = false")
    Optional<District> findByIdAndIsDeletedFalse(@Param("id") UUID id);

    @EntityGraph(attributePaths = {"province"})
    @Query("SELECT d FROM District d WHERE d.districtCode = :code AND d.isDeleted = false")
    Optional<District> findByDistrictCodeAndIsDeletedFalse(@Param("code") String districtCode);

    @EntityGraph(attributePaths = {"province"})
    @Query("SELECT d FROM District d WHERE d.districtEn = :nameEn AND d.isDeleted = false")
    Optional<District> findByDistrictEnAndIsDeletedFalse(@Param("nameEn") String districtEn);

    @EntityGraph(attributePaths = {"province"})
    @Query("SELECT d FROM District d WHERE d.districtKh = :nameKh AND d.isDeleted = false")
    Optional<District> findByDistrictKhAndIsDeletedFalse(@Param("nameKh") String districtKh);

    @EntityGraph(attributePaths = {"province"})
    @Query("SELECT d FROM District d " +
            "WHERE d.provinceCode = :provinceCode AND d.isDeleted = false " +
            "ORDER BY d.districtCode")
    List<District> findAllByProvinceCodeAndIsDeletedFalse(@Param("provinceCode") String provinceCode);

    @EntityGraph(attributePaths = {"province"})
    @Query("SELECT d FROM District d " +
            "WHERE d.isDeleted = false " +
            "AND (:provinceCode IS NULL OR :provinceCode = '' OR d.provinceCode = :provinceCode) " +
            "AND (:search IS NULL OR :search = '' OR " +
            "LOWER(d.districtCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(d.districtEn) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(d.districtKh) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<District> searchDistricts(@Param("provinceCode") String provinceCode,
                                   @Param("search") String search,
                                   Pageable pageable);
}