package com.emenu.features.user_management.dto.filter;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class BusinessUserFilterRequest extends UserFilterRequest {
    private String role;
    private String subscriptionStatus;
}