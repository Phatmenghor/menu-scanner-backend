package com.emenu.features.payment.dto.filter;

import com.emenu.shared.dto.BaseFilterRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ExchangeRateFilterRequest extends BaseFilterRequest {
    private Boolean isActive;
}