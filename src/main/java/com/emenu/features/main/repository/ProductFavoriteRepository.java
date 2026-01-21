package com.emenu.features.main.repository;

import com.emenu.features.main.models.ProductFavorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductFavoriteRepository extends JpaRepository<ProductFavorite, UUID> {

    boolean existsByUserIdAndProductIdAndIsDeletedFalse(UUID userId, UUID productId);

    Optional<ProductFavorite> findByUserIdAndProductIdAndIsDeletedFalse(UUID userId, UUID productId);

    @Query("SELECT pf.productId FROM ProductFavorite pf " +
            "WHERE pf.userId = :userId AND pf.productId IN :productIds AND pf.isDeleted = false")
    List<UUID> findFavoriteProductIdsByUserIdAndProductIds(@Param("userId") UUID userId,
                                                           @Param("productIds") List<UUID> productIds);

    @Modifying
    @Query("DELETE FROM ProductFavorite pf WHERE pf.id = :favoriteId")
    void deleteByFavoriteId(@Param("favoriteId") UUID favoriteId);

    @Modifying
    @Query("DELETE FROM ProductFavorite pf WHERE pf.userId = :userId AND pf.productId = :productId")
    void deleteByUserIdAndProductId(@Param("userId") UUID userId, @Param("productId") UUID productId);

    @Modifying
    @Query("DELETE FROM ProductFavorite pf WHERE pf.userId = :userId")
    int deleteAllByUserId(@Param("userId") UUID userId);
}