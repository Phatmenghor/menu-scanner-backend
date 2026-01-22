package com.emenu.features.location.repository;

import com.emenu.features.location.models.Commune;
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
public interface CommuneRepository extends JpaRepository<Commune, UUID> {

    /**
     * Checks if a non-deleted commune exists with the given commune code
     */
    boolean existsByCommuneCodeAndIsDeletedFalse(String communeCode);

    /**
     * Finds a non-deleted commune by ID with district and province eagerly fetched
     */
    @EntityGraph(attributePaths = {"district", "district.province"})
    @Query("SELECT c FROM Commune c WHERE c.id = :id AND c.isDeleted = false")
    Optional<Commune> findByIdAndIsDeletedFalse(@Param("id") UUID id);

    /**
     * Finds a non-deleted commune by commune code with district and province eagerly fetched
     */
    @EntityGraph(attributePaths = {"district", "district.province"})
    @Query("SELECT c FROM Commune c WHERE c.communeCode = :code AND c.isDeleted = false")
    Optional<Commune> findByCommuneCodeAndIsDeletedFalse(@Param("code") String communeCode);

    /**
     * Finds a non-deleted commune by English name with district and province eagerly fetched
     */
    @EntityGraph(attributePaths = {"district", "district.province"})
    @Query("SELECT c FROM Commune c WHERE c.communeEn = :nameEn AND c.isDeleted = false")
    Optional<Commune> findByCommuneEnAndIsDeletedFalse(@Param("nameEn") String communeEn);

    /**
     * Finds a non-deleted commune by Khmer name with district and province eagerly fetched
     */
    @EntityGraph(attributePaths = {"district", "district.province"})
    @Query("SELECT c FROM Commune c WHERE c.communeKh = :nameKh AND c.isDeleted = false")
    Optional<Commune> findByCommuneKhAndIsDeletedFalse(@Param("nameKh") String communeKh);

    /**
     * Finds all non-deleted communes by district code with district and province eagerly fetched
     */
    @EntityGraph(attributePaths = {"district", "district.province"})
    @Query("SELECT c FROM Commune c " +
            "WHERE c.districtCode = :districtCode AND c.isDeleted = false " +
            "ORDER BY c.communeCode")
    List<Commune> findAllByDistrictCodeAndIsDeletedFalse(@Param("districtCode") String districtCode);

    /**
     * Searches communes with filters for district, province, and text search across multiple fields
     */
    @EntityGraph(attributePaths = {"district", "district.province"})
    @Query("SELECT c FROM Commune c " +
            "JOIN c.district d " +
            "WHERE c.isDeleted = false " +
            "AND (:districtCode IS NULL OR :districtCode = '' OR c.districtCode = :districtCode) " +
            "AND (:provinceCode IS NULL OR :provinceCode = '' OR d.provinceCode = :provinceCode) " +
            "AND (:search IS NULL OR :search = '' OR " +
            "LOWER(c.communeCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(c.communeEn) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(c.communeKh) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Commune> searchCommunes(@Param("districtCode") String districtCode,
                                 @Param("provinceCode") String provinceCode,
                                 @Param("search") String search,
                                 Pageable pageable);
}