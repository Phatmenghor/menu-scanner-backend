package com.emenu.features.location.repository;

import com.emenu.features.location.models.Village;
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
public interface VillageRepository extends JpaRepository<Village, UUID> {

    /**
     * Checks if a non-deleted village exists with the given village code
     */
    boolean existsByVillageCodeAndIsDeletedFalse(String villageCode);

    /**
     * Finds a non-deleted village by ID with commune, district, and province eagerly fetched
     */
    @EntityGraph(attributePaths = {"commune", "commune.district", "commune.district.province"})
    @Query("SELECT v FROM Village v WHERE v.id = :id AND v.isDeleted = false")
    Optional<Village> findByIdAndIsDeletedFalse(@Param("id") UUID id);

    /**
     * Finds a non-deleted village by village code with commune, district, and province eagerly fetched
     */
    @EntityGraph(attributePaths = {"commune", "commune.district", "commune.district.province"})
    @Query("SELECT v FROM Village v WHERE v.villageCode = :code AND v.isDeleted = false")
    Optional<Village> findByVillageCodeAndIsDeletedFalse(@Param("code") String villageCode);

    /**
     * Finds a non-deleted village by English name with commune, district, and province eagerly fetched
     */
    @EntityGraph(attributePaths = {"commune", "commune.district", "commune.district.province"})
    @Query("SELECT v FROM Village v WHERE v.villageEn = :nameEn AND v.isDeleted = false")
    Optional<Village> findByVillageEnAndIsDeletedFalse(@Param("nameEn") String villageEn);

    /**
     * Finds a non-deleted village by Khmer name with commune, district, and province eagerly fetched
     */
    @EntityGraph(attributePaths = {"commune", "commune.district", "commune.district.province"})
    @Query("SELECT v FROM Village v WHERE v.villageKh = :nameKh AND v.isDeleted = false")
    Optional<Village> findByVillageKhAndIsDeletedFalse(@Param("nameKh") String villageKh);

    /**
     * Finds all non-deleted villages by commune code with commune, district, and province eagerly fetched
     */
    @EntityGraph(attributePaths = {"commune", "commune.district", "commune.district.province"})
    @Query("SELECT v FROM Village v " +
            "WHERE v.communeCode = :communeCode AND v.isDeleted = false " +
            "ORDER BY v.villageCode")
    List<Village> findAllByCommuneCodeAndIsDeletedFalse(@Param("communeCode") String communeCode);

    /**
     * Searches villages with filters for commune, district, province, and text search across multiple fields
     */
    @EntityGraph(attributePaths = {"commune", "commune.district", "commune.district.province"})
    @Query("SELECT v FROM Village v " +
            "JOIN v.commune c " +
            "JOIN c.district d " +
            "WHERE v.isDeleted = false " +
            "AND (:communeCode IS NULL OR :communeCode = '' OR v.communeCode = :communeCode) " +
            "AND (:districtCode IS NULL OR :districtCode = '' OR c.districtCode = :districtCode) " +
            "AND (:provinceCode IS NULL OR :provinceCode = '' OR d.provinceCode = :provinceCode) " +
            "AND (:search IS NULL OR :search = '' OR " +
            "LOWER(v.villageCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(v.villageEn) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(v.villageKh) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Village> searchVillages(@Param("communeCode") String communeCode,
                                 @Param("districtCode") String districtCode,
                                 @Param("provinceCode") String provinceCode,
                                 @Param("search") String search,
                                 Pageable pageable);
}