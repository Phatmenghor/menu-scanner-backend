package com.emenu.features.user_management.dto.filter;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class CustomerFilterRequest extends UserFilterRequest {
    private String customerTier;
    private Integer minLoyaltyPoints;
    private Integer maxLoyaltyPoints;
}
