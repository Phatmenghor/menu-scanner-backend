package com.emenu.features.auth.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessOwnerChangePlanRequest {
    
    @NotNull(message = "New plan ID is required")
    private UUID newPlanId;
    private Boolean keepCurrentEndDate;
    private BigDecimal paymentAmount;
    private String paymentMethod;
    private String paymentReference;
    private String paymentNotes;
    
    public boolean hasPaymentInfo() {
        return paymentAmount != null;
    }
    
    public boolean isPaymentInfoComplete() {
        return paymentAmount != null && paymentMethod != null;
    }
    
    public boolean shouldKeepCurrentEndDate() {
        return keepCurrentEndDate != null && keepCurrentEndDate;
    }
}