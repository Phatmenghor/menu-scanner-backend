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
    
    private CommuneResponse commune;
}