package com.emenu.features.main.repository;

import com.emenu.features.main.models.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BannerRepository extends JpaRepository<Banner, UUID>, JpaSpecificationExecutor<Banner> {
    Optional<Banner> findByIdAndIsDeletedFalse(UUID id);
    
    @Query("SELECT b FROM Banner b " +
           "LEFT JOIN FETCH b.business " +
           "WHERE b.id = :id AND b.isDeleted = false")
    Optional<Banner> findByIdWithBusiness(@Param("id") UUID id);
}