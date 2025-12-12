package com.emenu.features.location.dto.response;

import com.emenu.shared.dto.BaseAuditResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class CommuneResponse extends BaseAuditResponse {
    private String communeCode;
    private String communeEn;
    private String communeKh;
    private String districtCode;
    
    // Nested district information
    private DistrictResponse district;
    
    // Nested province information
    private ProvinceResponse province;
}
