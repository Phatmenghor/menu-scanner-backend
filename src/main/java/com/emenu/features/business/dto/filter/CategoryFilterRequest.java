package com.emenu.features.business.dto.filter;

import com.emenu.enums.common.Status;
import com.emenu.features.business.dto.filter.base.CategoryFilterBase;
import com.emenu.shared.dto.BaseFilterRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class CategoryFilterRequest extends BaseFilterRequest implements CategoryFilterBase {
    private UUID businessId;
    private Status status;
}

