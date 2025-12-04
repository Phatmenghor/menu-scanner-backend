package com.emenu.features.auth.dto.filter;

import com.emenu.enums.user.AccountStatus;
import com.emenu.enums.user.RoleEnum;
import com.emenu.enums.user.UserType;
import com.emenu.shared.dto.BaseFilterRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserFilterRequest extends BaseFilterRequest {
    private UUID businessId;
    private List<AccountStatus> accountStatuses;  // Changed to List
    private List<UserType> userTypes;             // Changed to List
    private List<RoleEnum> roles;
}