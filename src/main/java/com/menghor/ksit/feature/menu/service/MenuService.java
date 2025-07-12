package com.menghor.ksit.feature.menu.service;

import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.feature.menu.dto.request.UserMenuUpdateDto;
import com.menghor.ksit.feature.menu.dto.response.MenuItemResponseDto;
import com.menghor.ksit.feature.menu.dto.response.UserMenuResponseDto;

import java.util.List;

public interface MenuService {
    
    /**
     * Get all menu items structure (admin view)
     */
    List<MenuItemResponseDto> getAllMenuItems();
    
    /**
     * Get ALL menus with user's permission status
     * This returns ALL menus with canView flags for easy frontend filtering
     */
    List<UserMenuResponseDto> getAllMenusWithPermissions(Long userId);
    
    /**
     * Get menus by role (shows default role permissions)
     */
    List<UserMenuResponseDto> getMenusByRole(RoleEnum role);
    
    /**
     * Update user menu permissions (survey-like pattern)
     * Creates/updates/soft deletes user-specific permissions
     */
    List<UserMenuResponseDto> updateUserMenuPermissions(Long userId, UserMenuUpdateDto updateDto);
    
    /**
     * Get user's viewable menus only (for navigation)
     * This is the filtered version for actual menu display
     */
    List<UserMenuResponseDto> getUserViewableMenus(Long userId);
    
    /**
     * Reset user's custom permissions to role defaults
     */
    List<UserMenuResponseDto> resetUserMenusToDefault(Long userId);

    void initializeMenuPermissionsForAllExistingUsers();

    List<UserMenuResponseDto> refreshUserMenuPermissionsAfterRoleChange(Long userId);
}