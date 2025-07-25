package com.emenu.features.payment.dto.filter;

import com.emenu.enums.payment.PaymentMethod;
import com.emenu.enums.payment.PaymentStatus;
import com.emenu.shared.dto.BaseFilterRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class PaymentFilterRequest extends BaseFilterRequest {
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private UUID businessId;
    private UUID planId;

    private LocalDateTime createdFrom;
    private LocalDateTime createdTo;
}