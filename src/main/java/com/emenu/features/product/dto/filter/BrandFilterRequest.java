package com.emenu.features.product.dto.filter;

import com.emenu.enums.common.Status;
import com.emenu.shared.dto.BaseFilterRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class BrandFilterRequest extends BaseFilterRequest {
    private UUID businessId;
    private Status status;
}