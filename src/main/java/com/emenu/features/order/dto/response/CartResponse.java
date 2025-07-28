package com.emenu.features.order.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class CartResponse {
    private UUID userId;
    private UUID businessId;
    private String businessName;
    private List<CartItemResponse> items;
    private Integer totalItems;
    private BigDecimal subtotal;
    private BigDecimal totalDiscount;
    private BigDecimal finalTotal;
    private LocalDateTime lastUpdated;
    private Boolean isEmpty;
    
    // Cart health information
    private Integer availableItems; // Count of items that are available
    private Integer unavailableItems; // Count of items that are no longer available
    private Boolean hasUnavailableItems; // Quick check if any items are unavailable
    private Boolean hasPromotions; // Quick check if any items have active promotions
    
    // Warning messages for frontend
    private List<String> warnings; // e.g., "Some items are no longer available"
}