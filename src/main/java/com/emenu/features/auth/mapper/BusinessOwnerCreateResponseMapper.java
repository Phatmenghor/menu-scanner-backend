package com.emenu.features.auth.mapper;

import com.emenu.features.auth.dto.response.BusinessOwnerCreateResponse;
import com.emenu.features.auth.dto.response.BusinessResponse;
import com.emenu.features.auth.dto.response.UserResponse;
import com.emenu.features.payment.dto.response.PaymentResponse;
import com.emenu.features.subdomain.dto.response.SubdomainResponse;
import com.emenu.features.subscription.dto.response.SubscriptionResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class BusinessOwnerCreateResponseMapper {

    /**
     * ✅ Create comprehensive BusinessOwnerCreateResponse
     * Subscription is always created and never null
     */
    public BusinessOwnerCreateResponse create(
            UserResponse userResponse,
            BusinessResponse businessResponse,
            SubdomainResponse subdomainResponse,
            SubscriptionResponse subscriptionResponse, // Never null
            PaymentResponse paymentResponse) { // Can be null
        
        BusinessOwnerCreateResponse response = new BusinessOwnerCreateResponse();
        
        // ✅ Set the main components (subscription always present)
        response.setOwner(userResponse);
        response.setBusiness(businessResponse);
        response.setSubdomain(subdomainResponse);
        response.setSubscription(subscriptionResponse); // Always set, never null
        response.setPayment(paymentResponse); // Can be null
        response.setCreatedAt(LocalDateTime.now());
        
        // ✅ Set boolean flags (only payment can be null)
        response.setHasPayment(paymentResponse != null);
        
        // ✅ Build created components list
        response.setCreatedComponents(buildCreatedComponentsList(
            userResponse, businessResponse, subdomainResponse, subscriptionResponse, paymentResponse));
        
        // ✅ Build comprehensive summary
        response.setSummary(buildComprehensiveSummary(
            userResponse, businessResponse, subdomainResponse, subscriptionResponse, paymentResponse));
        
        return response;
    }

    /**
     * ✅ Build list of created components
     */
    private List<String> buildCreatedComponentsList(
            UserResponse userResponse,
            BusinessResponse businessResponse,
            SubdomainResponse subdomainResponse,
            SubscriptionResponse subscriptionResponse,
            PaymentResponse paymentResponse) {
        
        List<String> components = new ArrayList<>();
        
        if (userResponse != null) {
            components.add("Business Owner (" + userResponse.getFullName() + ")");
        }
        
        if (businessResponse != null) {
            components.add("Business Profile (" + businessResponse.getName() + ")");
        }
        
        if (subdomainResponse != null) {
            components.add("Subdomain (" + subdomainResponse.getSubdomain() + ".menu.com)");
        }
        
        // ✅ Subscription is always created
        if (subscriptionResponse != null) {
            components.add("Subscription (" + subscriptionResponse.getPlanName() + ")");
        }
        
        // ✅ Payment is optional
        if (paymentResponse != null) {
            components.add("Payment Record (" + paymentResponse.getFormattedAmount() + ")");
        }
        
        return components;
    }

    /**
     * ✅ Build comprehensive summary text
     */
    private String buildComprehensiveSummary(
            UserResponse userResponse,
            BusinessResponse businessResponse,
            SubdomainResponse subdomainResponse,
            SubscriptionResponse subscriptionResponse,
            PaymentResponse paymentResponse) {
        
        StringBuilder summary = new StringBuilder();
        summary.append("✅ Business Owner Creation Completed Successfully\n\n");
        
        // ✅ Owner details
        if (userResponse != null) {
            summary.append("👤 Owner: ").append(userResponse.getFullName())
                   .append(" (").append(userResponse.getUserIdentifier()).append(")\n");
        }
        
        // ✅ Business details
        if (businessResponse != null) {
            summary.append("🏢 Business: ").append(businessResponse.getName()).append("\n");
        }
        
        // ✅ Subdomain details
        if (subdomainResponse != null) {
            summary.append("🌐 Subdomain: ").append(subdomainResponse.getFullUrl()).append("\n");
        }
        
        // ✅ Subscription details (always present)
        if (subscriptionResponse != null) {
            summary.append("📋 Subscription: ").append(subscriptionResponse.getPlanName());
            if (subscriptionResponse.getDaysRemaining() != null) {
                summary.append(" (").append(subscriptionResponse.getDaysRemaining()).append(" days remaining)");
            }
            summary.append("\n");
        }
        
        // ✅ Payment details (optional)
        if (paymentResponse != null) {
            summary.append("💳 Payment: ").append(paymentResponse.getFormattedAmount())
                   .append(" via ").append(paymentResponse.getPaymentMethod())
                   .append(" [").append(paymentResponse.getReferenceNumber()).append("]\n");
        } else {
            summary.append("💳 Payment: No payment record created\n");
        }
        
        summary.append("\n🎉 Business owner setup completed with ")
               .append(getComponentCount(userResponse, businessResponse, subdomainResponse, subscriptionResponse, paymentResponse))
               .append(" components!");
        
        return summary.toString();
    }

    /**
     * ✅ Count total components created
     */
    private int getComponentCount(
            UserResponse userResponse,
            BusinessResponse businessResponse,
            SubdomainResponse subdomainResponse,
            SubscriptionResponse subscriptionResponse,
            PaymentResponse paymentResponse) {
        
        int count = 0;
        if (userResponse != null) count++;
        if (businessResponse != null) count++;
        if (subdomainResponse != null) count++;
        if (subscriptionResponse != null) count++; // Always present
        if (paymentResponse != null) count++;
        
        return count;
    }

    /**
     * ✅ Create minimal response (for error cases or partial creation)
     */
    public BusinessOwnerCreateResponse createMinimal(
            UserResponse userResponse,
            BusinessResponse businessResponse,
            String errorMessage) {
        
        BusinessOwnerCreateResponse response = new BusinessOwnerCreateResponse();
        response.setOwner(userResponse);
        response.setBusiness(businessResponse);
        response.setCreatedAt(LocalDateTime.now());
        response.setHasPayment(false);
        
        List<String> components = new ArrayList<>();
        if (userResponse != null) components.add("Business Owner");
        if (businessResponse != null) components.add("Business Profile");
        
        response.setCreatedComponents(components);
        response.setSummary("⚠️ Partial creation completed. " + errorMessage);
        
        return response;
    }
}