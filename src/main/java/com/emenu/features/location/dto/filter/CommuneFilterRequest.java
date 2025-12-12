package com.emenu.features.location.dto.filter;

import com.emenu.shared.dto.BaseFilterRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class CommuneFilterRequest extends BaseFilterRequest {
    private String districtCode;  // Filter by district code
    private String provinceCode;  // Optional: Filter by province code
}