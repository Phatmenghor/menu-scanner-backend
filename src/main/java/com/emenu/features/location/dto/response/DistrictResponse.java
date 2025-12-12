package com.emenu.features.location.dto.response;

import com.emenu.shared.dto.BaseAuditResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class DistrictResponse extends BaseAuditResponse {
    private String districtCode;
    private String districtEn;
    private String districtKh;
    private String provinceCode;
    
    // Nested province information
    private ProvinceResponse province;
}
