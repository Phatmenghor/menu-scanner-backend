package com.menghor.ksit.feature.auth.dto.update;

import com.menghor.ksit.enumations.RoleEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class UserRoleUpdateRequestDto {
    
    @NotNull(message = "Roles list cannot be null")
    private List<RoleEnum> roles;
}