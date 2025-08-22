// src/main/java/com/emenu/features/auth/mapper/AuthMapper.java
package com.emenu.features.auth.mapper;

import com.emenu.features.auth.dto.response.LoginResponse;
import com.emenu.features.auth.models.Role;
import com.emenu.features.auth.models.User;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class AuthMapper {

    // ===== LOGIN RESPONSE MAPPING =====
    
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
    @Mapping(target = "hasTelegramLinked", expression = "java(user.hasTelegramLinked())")
    @Mapping(target = "telegramDisplayName", expression = "java(user.getTelegramDisplayName())")
    public abstract LoginResponse toLoginResponse(User user, String token);

    // ===== MAPPING HELPER METHODS =====

    @Named("getFullName")
    protected String getFullName(User user) {
        if (user == null) return null;
        return user.getFullName();
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

    // ===== WELCOME MESSAGE CREATION =====

    protected String createWelcomeMessage(User user) {
        if (user == null) return "Welcome!";

        String timeOfDay = getTimeOfDayGreeting();
        String userName = user.getFirstName() != null ? user.getFirstName() :
                         user.getUserIdentifier() != null ? user.getUserIdentifier() : "User";

        String telegramStatus = user.hasTelegramLinked() ? " (Telegram linked)" : "";

        return String.format("%s, %s! Welcome to Cambodia E-Menu Platform%s",
                timeOfDay, userName, telegramStatus);
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

    // ===== AFTER MAPPING CUSTOMIZATIONS =====

    @AfterMapping
    protected void enhanceLoginResponse(@MappingTarget LoginResponse response, User user) {
        response.setAvailableFeatures(getAvailableFeatures(user));
    }

    private List<String> getAvailableFeatures(User user) {
        List<String> features = new java.util.ArrayList<>();
        
        // Common features
        features.add("Profile Management");
        features.add("Account Settings");
        
        // Telegram features
        if (user.hasTelegramLinked()) {
            features.add("Telegram Notifications");
            features.add("Telegram Login");
        } else {
            features.add("Link Telegram Account");
        }
        
        // User type specific features
        if (user.isBusinessUser()) {
            features.add("Business Management");
            features.add("Menu Management");
            features.add("Order Processing");
        } else if (user.isCustomer()) {
            features.add("Browse Menus");
            features.add("Place Orders");
            features.add("Order History");
        } else if (user.isPlatformUser()) {
            features.add("Platform Administration");
            features.add("User Management");
            features.add("Business Oversight");
        }
        
        return features;
    }
}