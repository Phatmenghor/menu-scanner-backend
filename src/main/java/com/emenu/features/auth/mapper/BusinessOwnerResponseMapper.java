package com.emenu.features.auth.mapper;

import com.emenu.features.auth.dto.response.BusinessOwnerCreateResponse;
import com.emenu.features.auth.dto.response.BusinessOwnerDetailResponse;
import com.emenu.features.auth.models.Business;
import com.emenu.features.auth.models.User;
import com.emenu.features.payment.models.Payment;
import com.emenu.features.subscription.models.Subscription;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class BusinessOwnerResponseMapper {
    
    public BusinessOwnerCreateResponse toCreateResponse(
            User owner,
            Business business,
            Subscription subscription,
            Payment payment) {
        
        List<String> components = new ArrayList<>();
        components.add("Owner User");
        components.add("Business Profile");
        components.add("Subscription");
        if (payment != null) {
            components.add("Payment");
        }
        
        return BusinessOwnerCreateResponse.builder()
                .ownerId(owner.getId())
                .ownerUserIdentifier(owner.getUserIdentifier())
                .ownerEmail(owner.getEmail())
                .ownerFullName(owner.getFullName())
                .businessId(business.getId())
                .businessName(business.getName())
                .businessEmail(business.getEmail())
                .businessStatus(business.getStatus().name())
                .subscriptionId(subscription.getId())
                .planName(subscription.getPlan().getName())
                .planPrice(subscription.getPlan().getPrice())
                .planDurationDays(subscription.getPlan().getDurationDays())
                .subscriptionStartDate(subscription.getStartDate())
                .subscriptionEndDate(subscription.getEndDate())
                .daysRemaining(subscription.getDaysRemaining())
                .paymentId(payment != null ? payment.getId() : null)
                .paymentAmount(payment != null ? payment.getAmount() : null)
                .paymentStatus(payment != null ? payment.getStatus().name() : null)
                .paymentMethod(payment != null ? payment.getPaymentMethod().name() : null)
                .createdComponents(components)
                .createdAt(owner.getCreatedAt())
                .build();
    }
    
    public BusinessOwnerDetailResponse toDetailResponse(User owner, Business business) {
        return BusinessOwnerDetailResponse.builder()
                .ownerId(owner.getId())
                .ownerUserIdentifier(owner.getUserIdentifier())
                .ownerEmail(owner.getEmail())
                .ownerFullName(owner.getFullName())
                .ownerPhone(owner.getPhoneNumber())
                .ownerAccountStatus(owner.getAccountStatus().name())
                .businessId(business.getId())
                .businessName(business.getName())
                .businessEmail(business.getEmail())
                .businessPhone(business.getPhone())
                .businessAddress(business.getAddress())
                .businessStatus(business.getStatus())
                .isSubscriptionActive(business.getIsSubscriptionActive())
                .businessCreatedAt(business.getCreatedAt())
                .createdAt(owner.getCreatedAt())
                .lastModifiedAt(owner.getUpdatedAt())
                .build();
    }
}