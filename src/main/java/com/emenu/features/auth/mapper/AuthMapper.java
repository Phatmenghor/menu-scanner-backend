package com.emenu.features.auth.mapper;

import com.emenu.features.auth.dto.response.LoginResponse;
import com.emenu.features.auth.models.Role;
import com.emenu.features.auth.models.User;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class AuthMapper {

    @Mapping(source = "user", target = "fullName", qualifiedByName = "getFullName")
    @Mapping(source = "user.roles", target = "roles", qualifiedByName = "rolesToStringList")
    @Mapping(source = "user.business.name", target = "businessName")
    @Mapping(source = "user.businessId", target = "businessId")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.userIdentifier", target = "userIdentifier")
    @Mapping(source = "user.email", target = "email")
    @Mapping(source = "user.userType", target = "userType")
    @Mapping(source = "user.profileImageUrl", target = "profileImageUrl")
    @Mapping(source = "token", target = "accessToken")
    @Mapping(target = "tokenType", constant = "Bearer")
    @Mapping(target = "welcomeMessage", expression = "java(createWelcomeMessage(user))")
    public abstract LoginResponse toLoginResponse(User user, String token);

    @Named("getFullName")
    protected String getFullName(User user) {
        if (user == null) return null;
        return user.getFirstName() + " " + user.getLastName();
    }

    @Named("rolesToStringList")
    protected List<String> rolesToStringList(List<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return List.of();
        }
        return roles.stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList());
    }

    protected String createWelcomeMessage(User user) {
        if (user == null) return "Welcome!";

        String timeOfDay = getTimeOfDayGreeting();
        String userName = user.getFirstName() != null ? user.getFirstName() : 
                         user.getUserIdentifier() != null ? user.getUserIdentifier() : "User";

        return String.format("%s, %s! Welcome to Cambodia E-Menu Platform", timeOfDay, userName);
    }

    private String getTimeOfDayGreeting() {
        int hour = java.time.LocalTime.now().getHour();
        if (hour < 12) {
            return "Good morning";
        } else if (hour < 17) {
            return "Good afternoon";
        } else {
            return "Good evening";
        }
    }
}