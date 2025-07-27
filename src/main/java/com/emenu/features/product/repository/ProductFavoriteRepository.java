package com.emenu.features.product.repository;

import com.emenu.features.product.models.ProductFavorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductFavoriteRepository extends JpaRepository<ProductFavorite, UUID> {
    
    Optional<ProductFavorite> findByUserIdAndProductId(UUID userId, UUID productId);
    
    @Query("SELECT pf FROM ProductFavorite pf " +
           "LEFT JOIN FETCH pf.product p " +
           "LEFT JOIN FETCH p.images " +
           "LEFT JOIN FETCH p.sizes " +
           "WHERE pf.userId = :userId AND pf.isDeleted = false " +
           "ORDER BY pf.createdAt DESC")
    List<ProductFavorite> findFavoritesByUserId(@Param("userId") UUID userId);
    
    boolean existsByUserIdAndProductId(UUID userId, UUID productId);
    
    void deleteByUserIdAndProductId(UUID userId, UUID productId);
    
    @Query("SELECT COUNT(pf) FROM ProductFavorite pf WHERE pf.productId = :productId AND pf.isDeleted = false")
    long countByProductId(@Param("productId") UUID productId);
}