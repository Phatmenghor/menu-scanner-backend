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
    private Boolean canAccess;
    private String subdomain;
    private String fullDomain;
    private String fullUrl;
    private UUID businessId;
    private String businessName;
    private String status;
    private String message;
    private LocalDateTime lastChecked;
    
    // Static factory methods for easy creation
    public static SubdomainCheckResponse accessible(String subdomain, String businessName, UUID businessId) {
        return SubdomainCheckResponse.builder()
                .canAccess(true)
                .subdomain(subdomain)
                .fullDomain(subdomain + ".menu.com")
                .fullUrl("https://" + subdomain + ".menu.com")
                .businessId(businessId)
                .businessName(businessName)
                .status("ACTIVE")
                .message("Domain is accessible")
                .lastChecked(LocalDateTime.now())
                .build();
    }
    
    public static SubdomainCheckResponse notFound(String subdomain) {
        return SubdomainCheckResponse.builder()
                .canAccess(false)
                .subdomain(subdomain)
                .fullDomain(subdomain + ".menu.com")
                .fullUrl("https://" + subdomain + ".menu.com")
                .status("NOT_FOUND")
                .message("Subdomain not found")
                .lastChecked(LocalDateTime.now())
                .build();
    }
    
    public static SubdomainCheckResponse subscriptionExpired(String subdomain, String businessName, UUID businessId) {
        return SubdomainCheckResponse.builder()
                .canAccess(false)
                .subdomain(subdomain)
                .fullDomain(subdomain + ".menu.com")
                .fullUrl("https://" + subdomain + ".menu.com")
                .businessId(businessId)
                .businessName(businessName)
                .status("SUBSCRIPTION_EXPIRED")
                .message("Subscription has expired")
                .lastChecked(LocalDateTime.now())
                .build();
    }
    
    public static SubdomainCheckResponse suspended(String subdomain, String businessName, UUID businessId) {
        return SubdomainCheckResponse.builder()
                .canAccess(false)
                .subdomain(subdomain)
                .fullDomain(subdomain + ".menu.com")
                .fullUrl("https://" + subdomain + ".menu.com")
                .businessId(businessId)
                .businessName(businessName)
                .status("SUSPENDED")
                .message("Domain is suspended")
                .lastChecked(LocalDateTime.now())
                .build();
    }
}