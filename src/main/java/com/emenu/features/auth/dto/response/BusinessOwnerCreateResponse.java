package com.emenu.features.auth.dto.response;

import com.emenu.features.payment.dto.response.PaymentResponse;
import com.emenu.features.subscription.dto.response.SubscriptionResponse;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class BusinessOwnerCreateResponse {
    private UserResponse owner;
    private BusinessResponse business;
    private SubscriptionResponse subscription;
    private PaymentResponse payment;

    private String summary;
    private List<String> createdComponents;
    private Boolean hasPayment;
    private LocalDateTime createdAt;
}