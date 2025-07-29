package com.emenu.features.auth.dto.response;

import com.emenu.features.payment.dto.response.PaymentResponse;
import com.emenu.features.subdomain.dto.response.SubdomainResponse;
import com.emenu.features.subscription.dto.response.SubscriptionResponse;
import lombok.Data;

import java.util.List;

@Data
public class BusinessOwnerCreateResponse {
    private UserResponse owner;
    private BusinessResponse business;
    private SubdomainResponse subdomain;
    private SubscriptionResponse subscription; // null if not created
    private PaymentResponse payment; // null if not created
    
    // Summary information
    private String summary;
    private List<String> createdComponents;
    private Boolean hasSubscription;
    private Boolean hasPayment;
}
