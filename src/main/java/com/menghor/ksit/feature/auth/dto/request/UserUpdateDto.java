package com.menghor.ksit.feature.auth.dto.request;

import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.enumations.Status;
import jakarta.validation.constraints.Email;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserUpdateDto {
    @Email(message = "Email should be valid")
    private String username;

    // Single role for backward compatibility
    private RoleEnum role;

    // Multiple roles support
    private List<RoleEnum> roles;

    private Status status;
}