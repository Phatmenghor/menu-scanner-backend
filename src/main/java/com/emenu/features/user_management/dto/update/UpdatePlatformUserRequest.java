package com.emenu.features.user_management.dto.update;

import lombok.Data;

@Data
public class UpdatePlatformUserRequest {
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String department;
    private String position;
}