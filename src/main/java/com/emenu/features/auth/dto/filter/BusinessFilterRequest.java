package com.emenu.features.auth.dto.filter;

import com.emenu.enums.user.BusinessStatus;
import com.emenu.shared.dto.BaseFilterRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class BusinessFilterRequest extends BaseFilterRequest {
    // Filter by business status
    private BusinessStatus status;

    // Filter by active subscription status
    private Boolean hasActiveSubscription;
}
