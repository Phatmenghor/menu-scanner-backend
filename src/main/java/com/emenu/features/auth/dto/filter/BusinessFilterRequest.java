package com.emenu.features.auth.dto.filter;

import com.emenu.enums.user.BusinessStatus;
import com.emenu.shared.dto.BaseFilterRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class BusinessFilterRequest extends BaseFilterRequest {
    
    private List<BusinessStatus> status;
    private Boolean hasActiveSubscription;
}