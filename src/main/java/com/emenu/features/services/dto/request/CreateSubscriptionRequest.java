package com.emenu.features.services.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CreateSubscriptionRequest {
    @NotNull
    private UUID userId;
    
    @NotNull
    private UUID planId;
    
    private LocalDateTime startDate = LocalDateTime.now();
    private Boolean autoRenew = true;
}