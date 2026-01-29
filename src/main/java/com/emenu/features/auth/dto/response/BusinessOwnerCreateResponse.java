package com.emenu.features.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessOwnerCreateResponse {
    
    private UUID ownerId;
    private String ownerUserIdentifier;
    private String ownerEmail;
    private String ownerFullName;
    private UUID businessId;
    private String businessName;
    private String businessEmail;
    private String businessStatus;
    private UUID subscriptionId;
    private String planName;
    private BigDecimal planPrice;
    private Integer planDurationDays;
    private LocalDateTime subscriptionStartDate;
    private LocalDateTime subscriptionEndDate;
    private Long daysRemaining;
    private UUID paymentId;
    private BigDecimal paymentAmount;
    private String paymentStatus;
    private String paymentMethod;
    private List<String> createdComponents;
    private LocalDateTime createdAt;
}