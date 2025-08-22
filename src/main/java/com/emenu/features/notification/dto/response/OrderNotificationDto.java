package com.emenu.features.notification.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class OrderNotificationDto {
    private UUID businessId;
    private String customerName;
    private String businessName;
    private String orderDetails;
    private String total;
    private String orderStatus;
}