package com.emenu.features.order.dto.filter;

import com.emenu.enums.payment.PaymentMethod;
import com.emenu.enums.payment.PaymentStatus;
import com.emenu.shared.dto.BaseFilterRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class BusinessOrderPaymentFilterRequest extends BaseFilterRequest {
    private UUID businessId;
    private List<PaymentStatus> statuses;
    private PaymentMethod paymentMethod;
    private String customerPaymentMethod;
    private String customerPhone;
    private Boolean isGuestOrder;
    private Boolean isPosOrder;
    private LocalDateTime createdFrom;
    private LocalDateTime createdTo;
}
