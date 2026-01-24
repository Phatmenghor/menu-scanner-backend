package com.emenu.features.auth.dto.update;

import com.emenu.enums.user.AccountStatus;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class UserUpdateRequest {
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String profileImageUrl;
    private AccountStatus accountStatus;
    private UUID businessId;
    private List<String> roles;
    private String position;
    private String address;
    private String notes;
}
