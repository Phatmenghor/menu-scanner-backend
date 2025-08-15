package com.emenu.features.order.dto.filter.base;

import com.emenu.enums.common.Status;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface DeliveryOptionFilterBase {
    UUID getBusinessId();
    List<Status> getStatuses();
    String getSearch();
    String getSortBy();
    String getSortDirection();
    BigDecimal getMinPrice();
    BigDecimal getMaxPrice();
    Boolean getIsDefault();
}
