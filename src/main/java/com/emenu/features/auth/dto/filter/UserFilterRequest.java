package com.emenu.features.auth.dto.filter;

import com.emenu.enums.auth.SocialProvider;
import com.emenu.enums.user.AccountStatus;
import com.emenu.enums.user.RoleEnum;
import com.emenu.enums.user.UserType;
import com.emenu.shared.dto.BaseFilterRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserFilterRequest extends BaseFilterRequest {

    // Basic filters
    private UUID businessId;
    private AccountStatus accountStatus;
    private UserType userType;
    private List<RoleEnum> roles;

    private SocialProvider socialProvider;

    // ✅ NEW: Telegram-specific filters
    private Boolean hasTelegram; // Filter users who have/don't have Telegram linked
    private Boolean telegramNotificationsEnabled; // Filter users who can receive Telegram notifications
}