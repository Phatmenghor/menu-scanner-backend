package com.emenu.features.order.dto.filter;

import com.emenu.enums.payment.PaymentMethod;
import com.emenu.enums.payment.PaymentStatus;
import com.emenu.shared.dto.BaseFilterRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class PaymentFilterRequest extends BaseFilterRequest {
    private List<PaymentMethod> paymentMethods;
    private List<PaymentStatus> statuses;
    private UUID businessId;
    private UUID planId;
    private LocalDate createdFrom;
    private LocalDate createdTo;
}