package com.emenu.features.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Pricing and costs breakdown for an order
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPricingInfo {
    private Integer totalItems;      // Number of items in order
    private BigDecimal subtotal;     // Total before discounts and fees
    private BigDecimal totalDiscount; // Total discounts applied
    private BigDecimal deliveryFee;  // Delivery charge
    private BigDecimal finalTotal;   // Total amount to pay (subtotal - discount + delivery)
}
