package com.emenu.features.auth.dto.request;

import com.emenu.enums.user.BusinessStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BusinessCreateRequest {
    
    @NotBlank(message = "Business name is required")
    private String name;
    private String email;
    private String phone;
    private String address;
    private String description;
    private BusinessStatus status;
}