package com.emenu.features.auth.dto.update;

import com.emenu.enums.user.BusinessStatus;
import lombok.Data;

@Data
public class BusinessUpdateRequest {
    
    private String name;
    private String email;
    private String phone;
    private String address;
    private String description;
    private BusinessStatus status;
}