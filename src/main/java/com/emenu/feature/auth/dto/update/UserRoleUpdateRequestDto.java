package com.emenu.feature.auth.dto.update;

import com.emenu.enumations.RoleEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class UserRoleUpdateRequestDto {
    
    @NotNull(message = "Roles list cannot be null")
    private List<RoleEnum> roles;
}