package com.emenu.features.order.dto;

import java.util.UUID;

/**
 * Projection interface for cart item quantity queries.
 * Using interface-based projection ensures reliable field mapping with Hibernate 6 + Spring Data JPA.
 */
public interface CartQuantityProjection {
    UUID getProductId();
    Long getTotalQuantity();
}
