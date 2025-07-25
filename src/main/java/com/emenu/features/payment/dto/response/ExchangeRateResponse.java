package com.emenu.features.payment.dto.response;

import com.emenu.shared.dto.BaseAuditResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class ExchangeRateResponse extends BaseAuditResponse {
    private Double usdToKhrRate;
    private Boolean isActive;
    private String notes;
}