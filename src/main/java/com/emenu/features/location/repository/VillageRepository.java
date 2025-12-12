package com.emenu.features.location.repository;

import com.emenu.features.location.models.Village;
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
public interface VillageRepository extends JpaRepository<Village, UUID> {
    @Query("SELECT v FROM Village v " +
           "LEFT JOIN FETCH v.commune c " +
           "LEFT JOIN FETCH c.district d " +
           "LEFT JOIN FETCH d.province " +
           "WHERE v.id = :id AND v.isDeleted = false")
    Optional<Village> findByIdAndIsDeletedFalse(@Param("id") UUID id);
    
    @Query("SELECT v FROM Village v " +
           "LEFT JOIN FETCH v.commune c " +
           "LEFT JOIN FETCH c.district d " +
           "LEFT JOIN FETCH d.province " +
           "WHERE v.villageCode = :code AND v.isDeleted = false")
    Optional<Village> findByVillageCodeAndIsDeletedFalse(@Param("code") String villageCode);
    
    @Query("SELECT v FROM Village v " +
           "LEFT JOIN FETCH v.commune c " +
           "LEFT JOIN FETCH c.district d " +
           "LEFT JOIN FETCH d.province " +
           "WHERE v.villageEn = :nameEn AND v.isDeleted = false")
    Optional<Village> findByVillageEnAndIsDeletedFalse(@Param("nameEn") String villageEn);
    
    @Query("SELECT v FROM Village v " +
           "LEFT JOIN FETCH v.commune c " +
           "LEFT JOIN FETCH c.district d " +
           "LEFT JOIN FETCH d.province " +
           "WHERE v.villageKh = :nameKh AND v.isDeleted = false")
    Optional<Village> findByVillageKhAndIsDeletedFalse(@Param("nameKh") String villageKh);
    
    boolean existsByVillageCodeAndIsDeletedFalse(String villageCode);
    
    @Query("SELECT v FROM Village v " +
           "LEFT JOIN FETCH v.commune c " +
           "LEFT JOIN FETCH c.district d " +
           "LEFT JOIN FETCH d.province " +
           "WHERE v.communeCode = :communeCode AND v.isDeleted = false")
    List<Village> findAllByCommuneCodeAndIsDeletedFalse(@Param("communeCode") String communeCode);
    
    @Query("SELECT v FROM Village v " +
           "LEFT JOIN FETCH v.commune c " +
           "LEFT JOIN FETCH c.district d " +
           "LEFT JOIN FETCH d.province " +
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
                                 @Param("search") String search, Pageable pageable);
}