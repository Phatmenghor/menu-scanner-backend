package com.menghor.ksit.feature.setting.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for user access to a menu item
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserMenuAccessDto {
    private Long userId;
    private String username;
    private String menuKey;
    private boolean hasAccess;
}