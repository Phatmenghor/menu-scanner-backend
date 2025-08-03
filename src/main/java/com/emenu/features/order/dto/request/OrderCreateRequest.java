package com.emenu.features.order.dto.request;

import com.emenu.enums.payment.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class OrderCreateRequest {
    
    @NotNull(message = "Business ID is required")
    private UUID businessId;
    
    // Guest customer info (for non-logged-in customers)
    private String guestPhone; // Required for guest orders
    private String guestName;
    private String guestLocation;
    
    // Delivery info (optional)
    private UUID deliveryAddressId;
    private UUID deliveryOptionId;
    
    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;
    
    private String customerPaymentMethod; // "Cash", "Card", "ABA Pay", etc.
    private String customerNote;
    
    // Order type flags
    private Boolean isPosOrder = false; // true when business creates for customer
    private Boolean isGuestOrder = false; // true when customer orders without login
}