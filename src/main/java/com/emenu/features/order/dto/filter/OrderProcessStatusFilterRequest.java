package com.emenu.features.order.dto.filter;

import com.emenu.enums.common.Status;
import com.emenu.shared.dto.BaseFilterRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class OrderProcessStatusFilterRequest extends BaseFilterRequest {
    private UUID businessId;
    private List<Status> statuses;
}
