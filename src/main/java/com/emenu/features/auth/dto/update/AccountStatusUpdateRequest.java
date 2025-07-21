package com.emenu.features.auth.dto.update;

import com.emenu.enums.AccountStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class AccountStatusUpdateRequest {
    
    @NotNull(message = "User ID is required")
    private UUID userId;
    
    @NotNull(message = "Account status is required")
    private AccountStatus accountStatus;
}