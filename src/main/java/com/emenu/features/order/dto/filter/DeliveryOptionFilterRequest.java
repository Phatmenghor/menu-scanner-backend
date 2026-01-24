package com.emenu.features.order.dto.filter;

import com.emenu.enums.common.Status;
import com.emenu.shared.dto.BaseFilterRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class DeliveryOptionFilterRequest extends BaseFilterRequest {
    private UUID businessId;
    private List<Status> statuses;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
}

