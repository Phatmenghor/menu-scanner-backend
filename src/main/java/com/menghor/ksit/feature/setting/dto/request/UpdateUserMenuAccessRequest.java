package com.menghor.ksit.feature.setting.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to grant/revoke a user's access to a menu item
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserMenuAccessRequest {
    private Long userId;
    private String menuKey;
    private boolean hasAccess;
}