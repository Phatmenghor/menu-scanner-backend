package com.emenu.features.user_management.dto.update;

import lombok.Data;

@Data
public class UpdateCustomerRequest {
    private String firstName;
    private String lastName;
    private String phoneNumber;
}
