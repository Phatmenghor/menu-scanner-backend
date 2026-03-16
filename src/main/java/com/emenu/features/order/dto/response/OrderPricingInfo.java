package com.emenu.features.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Pricing and costs breakdown for an order
 * Shows detailed breakdown: subtotal before discount -> subtotal after discount -> tax -> delivery -> final total
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPricingInfo {
    private Integer totalItems;          // Number of items in order
    private BigDecimal subtotalBeforeDiscount; // Sum of all items at original price
    private BigDecimal subtotal;         // Sum of all items after discount (subtotalBeforeDiscount - totalDiscount)
    private BigDecimal totalDiscount;    // Total discounts applied to items
    private BigDecimal deliveryFee;      // Delivery charge
    private BigDecimal taxAmount;        // Tax amount
    private BigDecimal finalTotal;       // Total amount to pay (subtotal - discount + delivery + tax)
}
