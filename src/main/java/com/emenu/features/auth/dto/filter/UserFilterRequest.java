package com.emenu.features.auth.dto.filter;

import com.emenu.enums.user.AccountStatus;
import com.emenu.enums.user.RoleEnum;
import com.emenu.enums.user.UserType;
import com.emenu.shared.dto.BaseFilterRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserFilterRequest extends BaseFilterRequest {

    // Filter by business ID
    private UUID businessId;

    // Filter by account status
    private AccountStatus accountStatus;

    // Filter by user type
    private UserType userType;

    private List<RoleEnum> roles;
}
