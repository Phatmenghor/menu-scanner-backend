package com.emenu.features.location.dto.filter;

import com.emenu.shared.dto.BaseFilterRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ProvinceFilterRequest extends BaseFilterRequest {
    // Inherits: search, pageNo, pageSize, sortBy, sortDirection
}