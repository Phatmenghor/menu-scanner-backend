package com.emenu.features.order.dto.filter;

import com.emenu.shared.dto.BaseFilterRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ExchangeRateFilterRequest extends BaseFilterRequest {
    private Boolean isActive;
}