package com.emenu.features.auth.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PaymentProcessRequest {
    
    @Size(max = 1000, message = "Reason cannot exceed 1000 characters")
    private String reason;
    
    @Size(max = 1000, message = "Admin notes cannot exceed 1000 characters")
    private String adminNotes;
    
    private String paymentProofUrl;
}