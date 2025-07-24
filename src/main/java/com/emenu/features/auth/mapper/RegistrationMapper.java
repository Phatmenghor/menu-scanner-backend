package com.emenu.features.auth.mapper;

import com.emenu.enums.user.UserType;
import com.emenu.features.auth.dto.request.RegisterRequest;
import com.emenu.features.auth.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class RegistrationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true) // Set separately after encoding
    @Mapping(target = "userType", ignore = true) // Set based on registration type
    @Mapping(target = "accountStatus", constant = "ACTIVE")
    @Mapping(target = "businessId", ignore = true)
    @Mapping(target = "business", ignore = true)
    @Mapping(target = "roles", ignore = true) // Set separately
    @Mapping(target = "position", ignore = true)
    @Mapping(target = "notes", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    public abstract User toEntity(RegisterRequest request);

    // Helper methods for different registration types
    public User toCustomerEntity(RegisterRequest request) {
        User user = toEntity(request);
        user.setUserType(UserType.CUSTOMER);
        return user;
    }

    public User toBusinessOwnerEntity(RegisterRequest request) {
        User user = toEntity(request);
        user.setUserType(UserType.BUSINESS_USER);
        return user;
    }

    public User toPlatformUserEntity(RegisterRequest request) {
        User user = toEntity(request);
        user.setUserType(UserType.PLATFORM_USER);
        return user;
    }
}