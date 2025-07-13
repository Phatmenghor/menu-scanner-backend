package com.menghor.ksit.feature.menu.service;

import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.feature.menu.dto.request.MenuBatchReorderDto;
import com.menghor.ksit.feature.menu.dto.request.MenuCreateDto;
import com.menghor.ksit.feature.menu.dto.request.MenuUpdateDto;
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

    /**
     * Initialize menu permissions for all existing users (system startup)
     */
    void initializeMenuPermissionsForAllExistingUsers();

    /**
     * Initialize menu permissions for a new user (called when user is created)
     */
    void initializeMenuPermissionsForNewUser(Long userId);

    /**
     * Refresh user menu permissions after role change
     */
    List<UserMenuResponseDto> refreshUserMenuPermissionsAfterRoleChange(Long userId);

    // ===== NEW MENU MANAGEMENT METHODS =====

    /**
     * Create a new menu item
     */
    MenuItemResponseDto createMenuItem(MenuCreateDto createDto);

    /**
     * Update an existing menu item
     */
    MenuItemResponseDto updateMenuItem(Long menuId, MenuUpdateDto updateDto);

    /**
     * Soft delete a menu item and handle user permissions
     */
    MenuItemResponseDto deleteMenuItem(Long menuId);

    /**
     * Reorder menu items (batch update)
     */
    List<MenuItemResponseDto> reorderMenuItems(MenuBatchReorderDto reorderDto);

    /**
     * Move menu item to specific position
     */
    MenuItemResponseDto moveMenuItem(Long menuId, Integer newPosition, Long newParentId);

    /**
     * Clean up deleted menu permissions for all users
     */
    void cleanupDeletedMenuPermissions();
}