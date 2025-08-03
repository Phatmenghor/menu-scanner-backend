package com.emenu.features.product.repository;

import com.emenu.features.product.models.ProductFavorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductFavoriteRepository extends JpaRepository<ProductFavorite, UUID>, JpaSpecificationExecutor<ProductFavorite> {
    
    @Query("SELECT pf FROM ProductFavorite pf " +
           "LEFT JOIN FETCH pf.product p " +
           "LEFT JOIN FETCH p.images " +
           "LEFT JOIN FETCH p.sizes " +
           "LEFT JOIN FETCH p.category " +
           "LEFT JOIN FETCH p.brand " +
           "WHERE pf.userId = :userId AND pf.isDeleted = false " +
           "ORDER BY pf.createdAt DESC")
    List<ProductFavorite> findFavoritesByUserId(@Param("userId") UUID userId);
    
    boolean existsByUserIdAndProductId(UUID userId, UUID productId);
    
    @Modifying
    @Query("DELETE FROM ProductFavorite pf WHERE pf.userId = :userId AND pf.productId = :productId")
    void deleteByUserIdAndProductId(@Param("userId") UUID userId, @Param("productId") UUID productId);
    
    @Modifying
    @Query("DELETE FROM ProductFavorite pf WHERE pf.userId = :userId")
    int deleteAllByUserId(@Param("userId") UUID userId);
    
    @Query("SELECT COUNT(pf) FROM ProductFavorite pf WHERE pf.userId = :userId AND pf.isDeleted = false")
    long countByUserId(@Param("userId") UUID userId);
    
    @Query("SELECT COUNT(pf) FROM ProductFavorite pf WHERE pf.productId = :productId AND pf.isDeleted = false")
    long countByProductId(@Param("productId") UUID productId);
}