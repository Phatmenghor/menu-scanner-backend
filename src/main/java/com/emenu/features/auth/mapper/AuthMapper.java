package com.emenu.features.auth.mapper;

import com.emenu.features.auth.dto.response.LoginResponse;
import com.emenu.features.auth.dto.response.TelegramLoginResponse;
import com.emenu.features.auth.models.Role;
import com.emenu.features.auth.models.User;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class AuthMapper {

    // ===== TRADITIONAL LOGIN RESPONSE =====
    
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

    // ===== TELEGRAM LOGIN RESPONSE =====
    
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.userIdentifier", target = "userIdentifier")
    @Mapping(source = "user.email", target = "email")
    @Mapping(source = "user", target = "fullName", qualifiedByName = "getFullName")
    @Mapping(source = "user", target = "displayName", qualifiedByName = "getDisplayName")
    @Mapping(source = "user.profileImageUrl", target = "profileImageUrl")
    @Mapping(source = "user.userType", target = "userType")
    @Mapping(source = "user.roles", target = "roles", qualifiedByName = "rolesToStringList")
    @Mapping(source = "user.businessId", target = "businessId")
    @Mapping(source = "user.business.name", target = "businessName")
    @Mapping(source = "user.socialProvider", target = "socialProvider")
    @Mapping(source = "user.telegramUserId", target = "telegramUserId")
    @Mapping(source = "user.telegramUsername", target = "telegramUsername")
    @Mapping(source = "user", target = "telegramDisplayName", qualifiedByName = "getTelegramDisplayName")
    @Mapping(source = "user.telegramLinkedAt", target = "telegramLinkedAt")
    @Mapping(source = "user.telegramNotificationsEnabled", target = "telegramNotificationsEnabled")
    @Mapping(source = "token", target = "accessToken")
    @Mapping(target = "tokenType", constant = "Bearer")
    @Mapping(target = "isNewUser", expression = "java(isNewUser)")
    @Mapping(target = "hasPasswordSet", expression = "java(user.getPassword() != null)")
    @Mapping(target = "welcomeMessage", expression = "java(createTelegramWelcomeMessage(user))")
    public abstract TelegramLoginResponse toTelegramLoginResponse(User user, String token, boolean isNewUser);

    // ===== MAPPING HELPER METHODS =====

    @Named("getFullName")
    protected String getFullName(User user) {
        if (user == null) return null;
        return user.getFullName();
    }

    @Named("getDisplayName")
    protected String getDisplayName(User user) {
        if (user == null) return null;
        return user.getDisplayName();
    }

    @Named("getTelegramDisplayName")
    protected String getTelegramDisplayName(User user) {
        if (user == null) return null;
        return user.getTelegramDisplayName();
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

    protected String createTelegramWelcomeMessage(User user) {
        if (user == null) return "Welcome via Telegram!";

        String userName = user.getDisplayName();
        String userType = user.getUserType().getDescription();
        
        return String.format("🎉 Welcome back, %s! You're logged in as %s via Telegram.", 
                userName, userType);
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
        // Add additional features based on user type and Telegram status
        response.setAvailableFeatures(getAvailableFeatures(user));
    }

    @AfterMapping
    protected void enhanceTelegramLoginResponse(@MappingTarget TelegramLoginResponse response, User user) {
        // Add available features specific to Telegram users
        response.setAvailableFeatures(getTelegramAvailableFeatures(user));
    }

    private List<String> getAvailableFeatures(User user) {
        List<String> features = new java.util.ArrayList<>();
        
        // Common features
        features.add("Profile Management");
        features.add("Account Settings");
        
        // Telegram features
        if (user.hasTelegramLinked()) {
            features.add("Telegram Notifications");
            features.add("Telegram Bot Commands");
        } else {
            features.add("Link Telegram Account");
        }
        
        // User type specific features
        if (user.isBusinessUser()) {
            features.add("Business Management");
            features.add("Menu Management");
            features.add("Staff Management");
            features.add("Order Processing");
        } else if (user.isCustomer()) {
            features.add("Browse Menus");
            features.add("Place Orders");
            features.add("Order History");
        } else if (user.isPlatformUser()) {
            features.add("Platform Administration");
            features.add("User Management");
            features.add("Business Oversight");
            features.add("System Analytics");
        }
        
        return features;
    }

    private List<String> getTelegramAvailableFeatures(User user) {
        List<String> features = new java.util.ArrayList<>();
        
        // Telegram specific features
        features.add("Telegram Login");
        features.add("Real-time Notifications");
        features.add("Bot Commands");
        features.add("Quick Actions");
        
        // User type specific Telegram features
        if (user.isBusinessUser()) {
            features.add("Business Notifications");
            features.add("Order Alerts");
        } else if (user.isCustomer()) {
            features.add("Order Status Updates");
            features.add("Promotional Notifications");
        } else if (user.isPlatformUser()) {
            features.add("System Alerts");
            features.add("Admin Notifications");
            features.add("Platform Statistics");
        }
        
        return features;
    }
}