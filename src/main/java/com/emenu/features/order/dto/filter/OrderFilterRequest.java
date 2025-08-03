package com.emenu.features.order.dto.filter;

import com.emenu.enums.order.OrderStatus;
import com.emenu.enums.payment.PaymentMethod;
import com.emenu.shared.dto.BaseFilterRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class OrderFilterRequest extends BaseFilterRequest {
    private UUID businessId;
    private List<OrderStatus> statuses;
    private PaymentMethod paymentMethod;
    private Boolean isPaid;
    private Boolean isPosOrder;
    private Boolean isGuestOrder;
    private String customerPhone;
    private LocalDateTime createdFrom;
    private LocalDateTime createdTo;
    private LocalDateTime confirmedFrom;
    private LocalDateTime confirmedTo;
}