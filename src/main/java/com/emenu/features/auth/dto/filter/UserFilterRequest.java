package com.emenu.features.auth.dto.filter;

import com.emenu.enums.AccountStatus;
import com.emenu.enums.RoleEnum;
import com.emenu.enums.UserType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.util.UUID;

@Data
public class UserFilterRequest {

    // Search term (searches across email, firstName, lastName)
    private String search;

    // Filter by business ID
    private UUID businessId;

    // Filter by account status
    private AccountStatus accountStatus;

    // Filter by user type
    private UserType userType;

    @Min(value = 1, message = "Page number must be at least 1")
    private Integer pageNo = 1;

    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 100, message = "Page size cannot exceed 100")
    private Integer pageSize = 10;

    private String sortBy = "createdAt";
    private String sortDirection = "DESC";
}
