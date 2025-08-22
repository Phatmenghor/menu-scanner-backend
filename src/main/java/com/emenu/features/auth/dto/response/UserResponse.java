// src/main/java/com/emenu/features/auth/dto/response/UserResponse.java
package com.emenu.features.auth.dto.response;

import com.emenu.enums.auth.SocialProvider;
import com.emenu.enums.user.AccountStatus;
import com.emenu.enums.user.RoleEnum;
import com.emenu.enums.user.UserType;
import com.emenu.shared.dto.BaseAuditResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserResponse extends BaseAuditResponse {
    
    // ===== CORE USER INFORMATION =====
    private String userIdentifier;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String displayName;
    private String phoneNumber;
    private String profileImageUrl;
    private UserType userType;
    private AccountStatus accountStatus;
    private List<RoleEnum> roles;
    private String position;
    private String address;
    private String notes;

    // ===== BUSINESS ASSOCIATION =====
    private UUID businessId;
    private String businessName;

    // ===== SOCIAL LOGIN & TELEGRAM INTEGRATION =====
    private SocialProvider socialProvider;
    private Boolean hasTelegramLinked;
    private Long telegramUserId;
    private String telegramUsername;
    private String telegramDisplayName;
    private LocalDateTime telegramLinkedAt;
    private Boolean telegramNotificationsEnabled;
    private Boolean canReceiveTelegramNotifications;
    
    // ===== COMPUTED PROPERTIES =====
    
    public boolean isTelegramUser() {
        return SocialProvider.TELEGRAM.equals(socialProvider);
    }
    
    public boolean isLocalUser() {
        return SocialProvider.LOCAL.equals(socialProvider);
    }
    
    public boolean isActive() {
        return AccountStatus.ACTIVE.equals(accountStatus);
    }
    
    public boolean isPlatformUser() {
        return UserType.PLATFORM_USER.equals(userType);
    }
    
    public boolean isBusinessUser() {
        return UserType.BUSINESS_USER.equals(userType);
    }
    
    public boolean isCustomer() {
        return UserType.CUSTOMER.equals(userType);
    }
    
    public String getAuthenticationMethod() {
        if (hasTelegramLinked != null && hasTelegramLinked) {
            if (isTelegramUser()) {
                return "Telegram Only";
            } else {
                return "Traditional + Telegram";
            }
        } else {
            return "Traditional";
        }
    }
}