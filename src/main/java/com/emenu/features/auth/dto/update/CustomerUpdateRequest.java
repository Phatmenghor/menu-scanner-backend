package com.emenu.features.auth.dto.update;

import com.emenu.enums.AccountStatus;
import lombok.Data;

@Data
public class CustomerUpdateRequest {
    
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String address;
    private String notes;
    private AccountStatus accountStatus;
}