package com.emenu.features.order.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
public class CartSummaryResponse {
    private UUID businessId;
    private String businessName;
    private List<CartItemResponse> items;
    private Integer totalItems;
    private BigDecimal subtotalBeforeDiscount;  // Sum of all items at original price
    private BigDecimal subtotal;                 // Sum of all items after discount
    private BigDecimal totalDiscount;            // Total discount on all items
    private BigDecimal finalTotal;               // subtotal + fees (delivery, tax)
}
