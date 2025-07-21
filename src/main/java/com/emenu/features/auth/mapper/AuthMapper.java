package com.emenu.features.auth.mapper;

import com.emenu.features.auth.dto.response.LoginResponse;
import com.emenu.features.auth.models.Role;
import com.emenu.features.auth.models.User;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public abstract class AuthMapper {

    @Mapping(target = "accessToken", source = "token")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "fullName", source = "user", qualifiedByName = "getFullName")
    @Mapping(target = "profileImageUrl", source = "user.profileImageUrl")
    @Mapping(target = "userType", source = "user.userType")
    @Mapping(target = "roles", source = "user.roles", qualifiedByName = "rolesToStringList")
    @Mapping(target = "businessId", source = "user.businessId")
    @Mapping(target = "businessName", source = "user.business.name")
    @Mapping(target = "welcomeMessage", ignore = true)
    public abstract LoginResponse toLoginResponse(User user, String token);

    @AfterMapping
    protected void setWelcomeMessage(@MappingTarget LoginResponse response, User user) {
        String message = switch (user.getUserType()) {
            case CUSTOMER -> "Welcome back, " + user.getFirstName() + "! Ready to explore delicious food?";
            case BUSINESS_USER -> "Welcome back, " + user.getFirstName() + "! Let's manage your restaurant.";
            case PLATFORM_USER -> "Welcome back, " + user.getFirstName() + "! Platform administration ready.";
        };
        response.setWelcomeMessage(message);
    }

    protected String getFullName(User user) {
        return user.getFullName();
    }

    protected List<String> rolesToStringList(List<Role> roles) {
        if (roles == null) {
            return List.of();
        }
        return roles.stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList());
    }
}