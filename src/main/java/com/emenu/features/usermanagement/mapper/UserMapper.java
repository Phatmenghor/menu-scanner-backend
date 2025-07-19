package com.emenu.features.usermanagement.mapper;

import com.emenu.features.usermanagement.domain.User;
import com.emenu.features.usermanagement.dto.request.CreateUserRequest;
import com.emenu.features.usermanagement.dto.response.UserResponse;
import com.emenu.features.usermanagement.dto.response.UserSummaryResponse;
import com.emenu.features.usermanagement.dto.update.UpdateUserRequest;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "isDeleted", constant = "false")
    @Mapping(target = "password", ignore = true) // Will be set manually after encoding
    @Mapping(target = "accountStatus", source = "accountStatus", defaultValue = "PENDING_VERIFICATION")
    @Mapping(target = "emailVerified", constant = "false")
    @Mapping(target = "phoneVerified", constant = "false")
    @Mapping(target = "loyaltyPoints", constant = "0")
    @Mapping(target = "totalOrders", constant = "0")
    @Mapping(target = "totalSpent", constant = "0.0")
    @Mapping(target = "loginAttempts", constant = "0")
    @Mapping(target = "sessionCount", constant = "0")
    @Mapping(target = "totalLoginTime", constant = "0L")
    @Mapping(target = "twoFactorEnabled", constant = "false")
    @Mapping(target = "customerTier", constant = "BRONZE")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    User toEntity(CreateUserRequest request);

    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    @Mapping(target = "displayName", expression = "java(user.getDisplayName())")
    @Mapping(target = "roles", expression = "java(mapRolesToStrings(user))")
    @Mapping(target = "permissions", expression = "java(mapPermissionsToStrings(user))")
    @Mapping(target = "businessName", source = "company")
    @Mapping(target = "hasActiveSubscription", expression = "java(user.hasActiveSubscription())")
    @Mapping(target = "daysUntilExpiration", expression = "java(user.getDaysUntilSubscriptionExpires())")
    UserResponse toResponse(User user);

    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    @Mapping(target = "displayName", expression = "java(user.getDisplayName())")
    @Mapping(target = "businessName", source = "company")
    @Mapping(target = "hasActiveSubscription", expression = "java(user.hasActiveSubscription())")
    @Mapping(target = "roles", expression = "java(mapRolesToStrings(user))")
    UserSummaryResponse toSummaryResponse(User user);

    List<UserResponse> toResponseList(List<User> users);
    List<UserSummaryResponse> toSummaryResponseList(List<User> users);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "accessibleBusinessIds", ignore = true) // Handle separately with permission check
    void updateEntityFromRequest(UpdateUserRequest request, @MappingTarget User user);

    // Custom mapping methods
    default List<String> mapRolesToStrings(User user) {
        if (user.getRoles() == null) {
            return List.of();
        }
        return user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList());
    }

    default List<String> mapPermissionsToStrings(User user) {
        if (user.getRoles() == null) {
            return List.of();
        }
        return user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .distinct()
                .collect(Collectors.toList());
    }

    // Custom mapping for public profile - only mask sensitive data
    @Named("publicProfile")
    @Mapping(target = "email", source = "email", qualifiedByName = "maskEmailAddress")
    @Mapping(target = "phoneNumber", source = "phoneNumber", qualifiedByName = "maskPhoneDigits")
    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    @Mapping(target = "displayName", expression = "java(user.getDisplayName())")
    @Mapping(target = "roles", expression = "java(mapRolesToStrings(user))")
    @Mapping(target = "permissions", expression = "java(mapPermissionsToStrings(user))")
    @Mapping(target = "businessName", source = "company")
    @Mapping(target = "hasActiveSubscription", expression = "java(user.hasActiveSubscription())")
    @Mapping(target = "daysUntilExpiration", expression = "java(user.getDaysUntilSubscriptionExpires())")
    UserResponse toPublicProfileResponse(User user);

    // Admin view - full access
    @Named("adminView")
    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    @Mapping(target = "displayName", expression = "java(user.getDisplayName())")
    @Mapping(target = "roles", expression = "java(mapRolesToStrings(user))")
    @Mapping(target = "permissions", expression = "java(mapPermissionsToStrings(user))")
    @Mapping(target = "businessName", source = "company")
    @Mapping(target = "hasActiveSubscription", expression = "java(user.hasActiveSubscription())")
    @Mapping(target = "daysUntilExpiration", expression = "java(user.getDaysUntilSubscriptionExpires())")
    UserResponse toAdminResponse(User user);

    // Analytics view - limited data for UserSummaryResponse
    @Named("analyticsView")
    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    @Mapping(target = "displayName", expression = "java(user.getDisplayName())")
    @Mapping(target = "businessName", source = "company")
    @Mapping(target = "hasActiveSubscription", expression = "java(user.hasActiveSubscription())")
    @Mapping(target = "roles", expression = "java(mapRolesToStrings(user))")
    UserSummaryResponse toAnalyticsResponse(User user);

    // Export view - clean data without sensitive fields
    @Named("exportView")
    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    @Mapping(target = "displayName", expression = "java(user.getDisplayName())")
    @Mapping(target = "roles", expression = "java(mapRolesToStrings(user))")
    @Mapping(target = "permissions", expression = "java(mapPermissionsToStrings(user))")
    @Mapping(target = "businessName", source = "company")
    @Mapping(target = "hasActiveSubscription", expression = "java(user.hasActiveSubscription())")
    @Mapping(target = "daysUntilExpiration", expression = "java(user.getDaysUntilSubscriptionExpires())")
    UserResponse toExportResponse(User user);

    // Utility methods for data masking
    @Named("maskEmailAddress")
    default String maskEmailAddress(String email) {
        if (email == null || email.length() < 3) return email;
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) return email;

        String prefix = email.substring(0, 1) + "*".repeat(Math.max(0, atIndex - 2)) + email.substring(atIndex - 1);
        return prefix;
    }

    @Named("maskPhoneDigits")
    default String maskPhoneDigits(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 4) return phoneNumber;

        String digits = phoneNumber.replaceAll("[^0-9]", "");
        if (digits.length() < 4) return phoneNumber;

        String masked = phoneNumber.substring(0, phoneNumber.length() - 4)
                + "*".repeat(4);
        return masked;
    }
}