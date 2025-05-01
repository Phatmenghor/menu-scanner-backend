package com.menghor.ksit.feature.auth.dto.request;

import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.enumations.Status;
import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UserUpdateDto {
    @Email(message = "Email should be valid")
    private String username;
    
    private RoleEnum role;
    
    private Status status;
}