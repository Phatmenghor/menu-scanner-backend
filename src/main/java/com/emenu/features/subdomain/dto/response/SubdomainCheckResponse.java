package com.emenu.features.subdomain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubdomainCheckResponse {
    private Boolean isValid;
    private Boolean hasActiveSubscription;
    private Boolean canAccess;
    private String subdomain;
    private String fullDomain;
    private UUID businessId;
    private String businessName;
    private String subscriptionStatus;
    private String message;
    private String redirectUrl;
    private LocalDateTime lastChecked;
    
    // Static factory methods
    public static SubdomainCheckResponse accessible(String subdomain, String businessName, UUID businessId) {
        return SubdomainCheckResponse.builder()
                .isValid(true)
                .hasActiveSubscription(true)
                .canAccess(true)
                .subdomain(subdomain)
                .fullDomain(subdomain + ".menu.com")
                .businessId(businessId)
                .businessName(businessName)
                .subscriptionStatus("ACTIVE")
                .message("Domain is accessible")
                .lastChecked(LocalDateTime.now())
                .build();
    }
    
    public static SubdomainCheckResponse notFound(String subdomain) {
        return SubdomainCheckResponse.builder()
                .isValid(false)
                .hasActiveSubscription(false)
                .canAccess(false)
                .subdomain(subdomain)
                .fullDomain(subdomain + ".menu.com")
                .subscriptionStatus("NOT_FOUND")
                .message("Subdomain not found")
                .redirectUrl("https://menu.com/register")
                .lastChecked(LocalDateTime.now())
                .build();
    }
    
    public static SubdomainCheckResponse subscriptionExpired(String subdomain, String businessName, UUID businessId) {
        return SubdomainCheckResponse.builder()
                .isValid(true)
                .hasActiveSubscription(false)
                .canAccess(false)
                .subdomain(subdomain)
                .fullDomain(subdomain + ".menu.com")
                .businessId(businessId)
                .businessName(businessName)
                .subscriptionStatus("EXPIRED")
                .message("Subscription has expired")
                .redirectUrl("https://menu.com/subscription")
                .lastChecked(LocalDateTime.now())
                .build();
    }
    
    public static SubdomainCheckResponse suspended(String subdomain, String businessName, UUID businessId) {
        return SubdomainCheckResponse.builder()
                .isValid(true)
                .hasActiveSubscription(false)
                .canAccess(false)
                .subdomain(subdomain)
                .fullDomain(subdomain + ".menu.com")
                .businessId(businessId)
                .businessName(businessName)
                .subscriptionStatus("SUSPENDED")
                .message("Domain is suspended")
                .redirectUrl("https://menu.com/support")
                .lastChecked(LocalDateTime.now())
                .build();
    }
}