package com.emenu.features.auth.dto.response;

import com.emenu.features.payment.dto.response.PaymentResponse;
import com.emenu.features.subdomain.dto.response.SubdomainResponse;
import com.emenu.features.subscription.dto.response.SubscriptionResponse;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class BusinessOwnerCreateResponse {
    private UserResponse owner;
    private BusinessResponse business;
    private SubdomainResponse subdomain;
    private SubscriptionResponse subscription; // Always created, never null
    private PaymentResponse payment; // null if not created

    // Summary information
    private String summary;
    private List<String> createdComponents;
    private Boolean hasPayment;
    private LocalDateTime createdAt;

    // ✅ Simple convenience methods (no complex logic)
    public boolean hasAllComponents() {
        return owner != null && business != null && subdomain != null && subscription != null;
    }

    public boolean isFullSetup() {
        return hasAllComponents() && Boolean.TRUE.equals(hasPayment);
    }

    public int getComponentCount() {
        return createdComponents != null ? createdComponents.size() : 0;
    }
}