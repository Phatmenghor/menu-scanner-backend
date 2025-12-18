package com.emenu.features.auth.dto.filter;

import com.emenu.enums.payment.PaymentStatus;
import com.emenu.enums.user.AccountStatus;
import com.emenu.enums.user.BusinessStatus;
import com.emenu.shared.dto.BaseFilterRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class BusinessOwnerFilterRequest extends BaseFilterRequest {
    
    // Business filters
    private List<BusinessStatus> businessStatuses;
    
    // Owner account filters
    private List<AccountStatus> ownerAccountStatuses;
    
    // Subscription filters
    private List<String> subscriptionStatuses; // ACTIVE, EXPIRED, EXPIRING_SOON
    private Boolean autoRenew;
    private Integer expiringSoonDays = 7;
    
    // Payment filters
    private List<PaymentStatus> paymentStatuses;
}