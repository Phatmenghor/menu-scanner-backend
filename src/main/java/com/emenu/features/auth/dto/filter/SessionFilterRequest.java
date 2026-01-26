package com.emenu.features.auth.dto.filter;

import com.emenu.shared.dto.BaseFilterRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class SessionFilterRequest extends BaseFilterRequest {
    private UUID userId;
    private List<String> statuses;  // ACTIVE, EXPIRED, REVOKED, LOGGED_OUT
    private List<String> deviceTypes;  // WEB, MOBILE, TABLET, DESKTOP
}
