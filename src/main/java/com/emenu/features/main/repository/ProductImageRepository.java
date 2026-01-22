package com.emenu.features.main.repository;

import com.emenu.features.main.models.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, UUID> {

    /**
     * Finds all non-deleted product images by product ID, ordered by creation date descending
     */
    @Query("SELECT pi FROM ProductImage pi " +
            "WHERE pi.productId = :productId AND pi.isDeleted = false " +
            "ORDER BY pi.createdAt DESC")
    List<ProductImage> findByProductId(@Param("productId") UUID productId);
}