package com.menghor.ksit.feature.menu.service;

import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.feature.menu.dto.response.MenuItemResponseDto;
import com.menghor.ksit.feature.menu.dto.response.UserMenuResponseDto;
import com.menghor.ksit.feature.menu.dto.resquest.MenuReorderDto;
import com.menghor.ksit.feature.menu.dto.resquest.UserMenuPermissionCreateDto;
import com.menghor.ksit.feature.menu.dto.resquest.UserMenuReorderDto;
import com.menghor.ksit.feature.menu.dto.update.UserMenuPermissionUpdateDto;

import java.util.List;

public interface MenuService {
    
    // Get user's permitted menu items
    List<UserMenuResponseDto> getUserMenus(Long userId);
    
    // Get menu items by role
    List<UserMenuResponseDto> getMenusByRole(RoleEnum role);
    
    // Get all menu items (admin function)
    List<MenuItemResponseDto> getAllMenuItems();
    
    // Add custom menu permission for user
    UserMenuResponseDto addUserMenuPermission(UserMenuPermissionCreateDto createDto);
    
    // Update user menu permission
    UserMenuResponseDto updateUserMenuPermission(Long userMenuPermissionId, UserMenuPermissionUpdateDto updateDto);
    
    // Remove user menu permission (revert to default)
    void removeUserMenuPermission(Long userId, Long menuItemId);
    
    // Reorder menus for a specific user
    List<UserMenuResponseDto> reorderUserMenus(UserMenuReorderDto reorderDto);
    
    // Reorder default menus (admin function)
    List<MenuItemResponseDto> reorderDefaultMenus(MenuReorderDto reorderDto);
    
    // Initialize default menu permissions for a user
    void initializeUserMenuPermissions(Long userId);
}
