package com.emenu.features.order.dto;

import java.util.UUID;

/**
 * Projection interface for per-size cart item quantity queries.
 * Using interface-based projection ensures reliable field mapping with Hibernate 6 + Spring Data JPA.
 */
public interface SizeCartQuantityProjection {
    UUID getProductSizeId();
    Integer getQuantity();
}
