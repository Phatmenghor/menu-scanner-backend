package com.emenu.features.auth.mapper;

import com.emenu.features.auth.dto.response.BusinessOwnerCreateResponse;
import com.emenu.features.auth.dto.response.BusinessResponse;
import com.emenu.features.auth.dto.response.UserResponse;
import com.emenu.features.payment.dto.response.PaymentResponse;
import com.emenu.features.subscription.dto.response.SubscriptionResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class BusinessOwnerResponseMapper {

    public BusinessOwnerCreateResponse create(
            UserResponse owner,
            BusinessResponse business,
            SubscriptionResponse subscription,
            PaymentResponse payment) {

        BusinessOwnerCreateResponse response = new BusinessOwnerCreateResponse();
        response.setOwner(owner);
        response.setBusiness(business);
        response.setSubscription(subscription);
        response.setPayment(payment);
        response.setHasPayment(payment != null);
        response.setCreatedAt(LocalDateTime.now());

        List<String> components = new ArrayList<>();
        components.add("Business Owner Account");
        components.add("Business Profile");
        components.add("Subscription Plan");
        if (payment != null) {
            components.add("Initial Payment");
        }
        response.setCreatedComponents(components);

        String summary = String.format(
                "Successfully created business owner '%s' for business '%s' with subdomain '%s'",
                owner.getFullName(),
                business.getName()
        );
        response.setSummary(summary);

        return response;
    }
}