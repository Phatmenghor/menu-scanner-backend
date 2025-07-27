package com.emenu.features.subdomain.dto.filter;

import com.emenu.enums.subdomain.SubdomainStatus;
import com.emenu.shared.dto.BaseFilterRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class SubdomainFilterRequest extends BaseFilterRequest {
    private UUID businessId;
    private SubdomainStatus status;
    private Boolean hasActiveSubscription;
}