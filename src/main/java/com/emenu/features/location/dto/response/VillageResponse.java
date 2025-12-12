package com.emenu.features.location.dto.response;

import com.emenu.shared.dto.BaseAuditResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class VillageResponse extends BaseAuditResponse {
    private String villageCode;
    private String villageEn;
    private String villageKh;
    private String communeCode;
    
    // Nested commune information
    private CommuneResponse commune;
    
    // Nested district information
    private DistrictResponse district;
    
    // Nested province information
    private ProvinceResponse province;
}