package com.emenu.features.order.dto.request;

import com.emenu.enums.payment.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class POSOrderCreateRequest {
    
    // Customer info for POS order
    @NotBlank(message = "Customer phone is required")
    private String customerPhone;
    
    private String customerName;
    private String customerLocation;
    
    // Items for POS order
    @NotNull(message = "Order items are required")
    private List<POSOrderItemRequest> items;
    
    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;
    
    private String customerPaymentMethod; // How customer paid to business
    private String customerNote;
    private String businessNote;
}
