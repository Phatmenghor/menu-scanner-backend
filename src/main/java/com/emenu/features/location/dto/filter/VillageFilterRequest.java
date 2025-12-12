package com.emenu.features.location.dto.filter;

import com.emenu.shared.dto.BaseFilterRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class VillageFilterRequest extends BaseFilterRequest {
    private String communeCode;
}
