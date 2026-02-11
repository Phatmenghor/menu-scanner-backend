package com.emenu.features.order.dto.filter;

import com.emenu.enums.payment.PaymentMethod;
import com.emenu.enums.payment.PaymentStatus;
import com.emenu.shared.dto.BaseFilterRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class OrderFilterRequest extends BaseFilterRequest {
    private UUID businessId;
    private String orderProcessStatusName;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
}
