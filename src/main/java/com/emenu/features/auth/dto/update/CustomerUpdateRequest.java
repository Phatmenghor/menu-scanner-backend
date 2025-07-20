package com.emenu.features.auth.dto.update;

import lombok.Data;

@Data
public class CustomerUpdateRequest {
    
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String address;
}