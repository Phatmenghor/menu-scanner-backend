package com.menghor.ksit.feature.setting.service;

import com.menghor.ksit.feature.setting.dto.request.*;
import com.menghor.ksit.feature.setting.dto.response.MenuItemDto;
import com.menghor.ksit.feature.setting.dto.response.UserMenuAccessDto;
import com.menghor.ksit.feature.setting.dto.response.UserMenuDto;

import java.util.List;

public interface MenuService {

    /**
     * Get user's menu structure based on their access permissions
     * @param userId User ID
     * @return Hierarchical menu structure
     */
    UserMenuDto getUserMenu(Long userId);

    /**
     * Get current user's menu structure
     * @return Hierarchical menu structure
     */
    UserMenuDto getCurrentUserMenu();

    /**
     * Create a new menu item with positioning
     * @param createRequest Menu item creation request
     * @return Created menu item
     */
    MenuItemDto createMenuItem(CreateMenuItemRequest createRequest);

    /**
     * Update an existing menu item
     * @param id Menu item ID
     * @param updateRequest Updated menu item data
     * @return Updated menu item
     */
    MenuItemDto updateMenuItem(Long id, UpdateMenuItemRequest updateRequest);

    /**
     * Reposition a menu item
     * @param request Repositioning request with target position
     * @return Updated menu item
     */
    MenuItemDto repositionMenuItem(RepositionMenuItemRequest request);

    /**
     * Toggle menu item enabled status
     * @param menuKey Menu item key
     * @param enabled New enabled status
     * @return Updated menu item
     */
    MenuItemDto toggleMenuItemStatus(String menuKey, boolean enabled);

    /**
     * Get all menu items (for admin use)
     * @return All menu items
     */
    List<MenuItemDto> getAllMenuItems();

    /**
     * Delete a menu item
     * @param menuKey Menu item key
     */
    void deleteMenuItem(String menuKey);

    /**
     * Grant or revoke a user's access to a menu item
     * @param request Access update request
     * @return Updated access status
     */
    UserMenuAccessDto updateUserMenuAccess(UpdateUserMenuAccessRequest request);

    /**
     * Update menu access for multiple menu items at once
     * @param request Bulk access update request
     * @return Number of items updated
     */
    int bulkUpdateUserMenuAccess(BulkMenuAccessRequest request);

    /**
     * Get all menu items a user has access to
     * @param userId User ID
     * @return List of menu keys
     */
    List<String> getUserMenuAccess(Long userId);

    /**
     * Initialize default menu items and access
     */
    void initializeDefaultMenuItems();
}