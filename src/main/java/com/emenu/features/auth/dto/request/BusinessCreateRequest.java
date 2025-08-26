package com.emenu.features.auth.dto.request;

import com.emenu.enums.user.BusinessStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BusinessCreateRequest {
    
    @NotBlank(message = "Business name is required")
    private String name;
    
    @Email(message = "Email format is invalid")
    private String email;
    
    private String phone;
    private String address;
    private String description;
    private BusinessStatus status = BusinessStatus.ACTIVE;
}