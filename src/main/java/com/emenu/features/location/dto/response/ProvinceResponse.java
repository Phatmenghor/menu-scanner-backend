package com.emenu.features.location.dto.response;

import com.emenu.shared.dto.BaseAuditResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ProvinceResponse extends BaseAuditResponse {
    private String provinceCode;
    private String provinceEn;
    private String provinceKh;
}