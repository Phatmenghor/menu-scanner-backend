package com.emenu.features.auth.dto.response;

import com.emenu.enums.user.BusinessStatus;
import com.emenu.shared.dto.BaseAuditResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class BusinessResponse extends BaseAuditResponse {
    
    private String name;
    private String email;
    private String phone;
    private String address;
    private String description;
    private BusinessStatus status;
    private Boolean isSubscriptionActive;
    private Boolean hasActiveSubscription;
}
