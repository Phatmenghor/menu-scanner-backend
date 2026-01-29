package com.emenu.features.auth.dto.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class RoleDetailResponse extends RoleResponse {
    private String businessName;
}
