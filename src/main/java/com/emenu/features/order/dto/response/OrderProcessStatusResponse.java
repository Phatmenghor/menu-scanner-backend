package com.emenu.features.order.dto.response;

import com.emenu.enums.common.Status;
import com.emenu.shared.dto.BaseAuditResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class OrderProcessStatusResponse extends BaseAuditResponse {
    private UUID businessId;
    private String businessName;
    private String name;
    private String description;
    private Status status;
}
