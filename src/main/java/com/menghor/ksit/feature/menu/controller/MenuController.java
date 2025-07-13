package com.menghor.ksit.feature.menu.controller;

import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.exceptoins.response.ApiResponse;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.menu.dto.request.MenuBatchReorderDto;
import com.menghor.ksit.feature.menu.dto.request.MenuCreateDto;
import com.menghor.ksit.feature.menu.dto.request.MenuUpdateDto;
import com.menghor.ksit.feature.menu.dto.request.UserMenuUpdateDto;
import com.menghor.ksit.feature.menu.dto.response.MenuItemResponseDto;
import com.menghor.ksit.feature.menu.dto.response.UserMenuResponseDto;
import com.menghor.ksit.feature.menu.service.MenuService;
import com.menghor.ksit.utils.database.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/menus")
@RequiredArgsConstructor
@Slf4j
public class MenuController {

    private final MenuService menuService;
    private final SecurityUtils securityUtils;

    /**
     * Get current user's menu permissions - ALL menus with canView flags
     * Frontend can filter where canView == true for navigation
     */
    @GetMapping("/my-menus")
    public ApiResponse<List<UserMenuResponseDto>> getMyMenus() {
        log.info("Getting current user's menus with canView flags");
        UserEntity currentUser = securityUtils.getCurrentUser();
        List<UserMenuResponseDto> menus = menuService.getAllMenusWithPermissions(currentUser.getId());
        return ApiResponse.success("User menus retrieved successfully", menus);
    }

    /**
     * Get current user's viewable menus only (filtered version)
     * This is the clean version for navigation display
     */
    @GetMapping("/my-menus/viewable")
    public ApiResponse<List<UserMenuResponseDto>> getMyViewableMenus() {
        log.info("Getting current user's viewable menus only");
        UserEntity currentUser = securityUtils.getCurrentUser();
        List<UserMenuResponseDto> menus = menuService.getUserViewableMenus(currentUser.getId());
        return ApiResponse.success("User viewable menus retrieved successfully", menus);
    }

    /**
     * Get user menus by user ID (Admin/Developer only)
     */
    @GetMapping("/users/{userId}")
    public ApiResponse<List<UserMenuResponseDto>> getUserMenus(@PathVariable Long userId) {
        log.info("Getting all menus with permissions for user ID: {}", userId);
        List<UserMenuResponseDto> menus = menuService.getAllMenusWithPermissions(userId);
        return ApiResponse.success("User menus retrieved successfully", menus);
    }

    /**
     * Get menus by role (Admin/Developer only)
     */
    @GetMapping("/roles/{role}")
    public ApiResponse<List<UserMenuResponseDto>> getMenusByRole(@PathVariable RoleEnum role) {
        log.info("Getting menus for role: {}", role);
        List<UserMenuResponseDto> menus = menuService.getMenusByRole(role);
        return ApiResponse.success("Role menus retrieved successfully", menus);
    }

    /**
     * Get all menu items structure (Admin/Developer only)
     */
    @GetMapping("/all")
    public ApiResponse<List<MenuItemResponseDto>> getAllMenuItems() {
        log.info("Getting all menu items structure");
        List<MenuItemResponseDto> menus = menuService.getAllMenuItems();
        return ApiResponse.success("All menu items retrieved successfully", menus);
    }

    /**
     * Update user menu permissions by Admin/Developer (survey-like pattern)
     */
    @PutMapping("/users/{userId}/permissions")
    public ApiResponse<List<UserMenuResponseDto>> updateUserMenuPermissions(
            @PathVariable Long userId,
            @Valid @RequestBody UserMenuUpdateDto updateDto) {
        log.info("Admin/Developer updating menu permissions for user ID: {}", userId);
        List<UserMenuResponseDto> menus = menuService.updateUserMenuPermissions(userId, updateDto);
        return ApiResponse.success("Menu permissions updated successfully", menus);
    }

    /**
     * Update current user's menu preferences (users can modify their own)
     */
    @PutMapping("/my-menus/permissions")
    public ApiResponse<List<UserMenuResponseDto>> updateMyMenuPermissions(@Valid @RequestBody UserMenuUpdateDto updateDto) {
        UserEntity currentUser = securityUtils.getCurrentUser();
        log.info("User updating own menu permissions for user ID: {}", currentUser.getId());
        List<UserMenuResponseDto> menus = menuService.updateUserMenuPermissions(currentUser.getId(), updateDto);
        return ApiResponse.success("Your menu permissions updated successfully", menus);
    }

    /**
     * Reset user's menu permissions to role defaults (Admin/Developer only)
     */
    @PostMapping("/users/{userId}/reset")
    public ApiResponse<List<UserMenuResponseDto>> resetUserMenusToDefault(@PathVariable Long userId) {
        log.info("Admin/Developer resetting menu permissions to defaults for user ID: {}", userId);
        List<UserMenuResponseDto> menus = menuService.resetUserMenusToDefault(userId);
        return ApiResponse.success("Menu permissions reset to defaults successfully", menus);
    }

    /**
     * Reset current user's menu permissions to role defaults
     */
    @PostMapping("/my-menus/reset")
    public ApiResponse<List<UserMenuResponseDto>> resetMyMenusToDefault() {
        UserEntity currentUser = securityUtils.getCurrentUser();
        log.info("User resetting own menu permissions to defaults for user ID: {}", currentUser.getId());
        List<UserMenuResponseDto> menus = menuService.resetUserMenusToDefault(currentUser.getId());
        return ApiResponse.success("Your menu permissions reset to defaults successfully", menus);
    }

    /**
     * Refresh user menu permissions after role change (Admin/Developer only)
     */
    @PostMapping("/users/{userId}/refresh")
    public ApiResponse<List<UserMenuResponseDto>> refreshUserMenuPermissions(@PathVariable Long userId) {
        log.info("Admin/Developer refreshing menu permissions after role change for user ID: {}", userId);
        List<UserMenuResponseDto> menus = menuService.refreshUserMenuPermissionsAfterRoleChange(userId);
        return ApiResponse.success("Menu permissions refreshed successfully", menus);
    }

    // ===== MENU MANAGEMENT ENDPOINTS (Admin/Developer only) =====

    /**
     * Create a new menu item
     */
    @PostMapping("/items")
    public ApiResponse<MenuItemResponseDto> createMenuItem(@Valid @RequestBody MenuCreateDto createDto) {
        log.info("Creating new menu item with code: {}", createDto.getCode());
        MenuItemResponseDto menu = menuService.createMenuItem(createDto);
        return ApiResponse.success("Menu item created successfully", menu);
    }

    /**
     * Update an existing menu item
     */
    @PutMapping("/items/{menuId}")
    public ApiResponse<MenuItemResponseDto> updateMenuItem(
            @PathVariable Long menuId,
            @Valid @RequestBody MenuUpdateDto updateDto) {
        log.info("Updating menu item with ID: {}", menuId);
        MenuItemResponseDto menu = menuService.updateMenuItem(menuId, updateDto);
        return ApiResponse.success("Menu item updated successfully", menu);
    }

    /**
     * Soft delete a menu item
     */
    @DeleteMapping("/items/{menuId}")
    public ApiResponse<MenuItemResponseDto> deleteMenuItem(@PathVariable Long menuId) {
        log.info("Deleting menu item with ID: {}", menuId);
        MenuItemResponseDto menu = menuService.deleteMenuItem(menuId);
        return ApiResponse.success("Menu item deleted successfully", menu);
    }

    /**
     * Reorder multiple menu items (batch update)
     */
    @PutMapping("/items/reorder")
    public ApiResponse<List<MenuItemResponseDto>> reorderMenuItems(@Valid @RequestBody MenuBatchReorderDto reorderDto) {
        log.info("Reordering {} menu items", reorderDto.getMenuReorders().size());
        List<MenuItemResponseDto> menus = menuService.reorderMenuItems(reorderDto);
        return ApiResponse.success("Menu items reordered successfully", menus);
    }

    /**
     * Move a single menu item to new position/parent
     */
    @PutMapping("/items/{menuId}/move")
    public ApiResponse<MenuItemResponseDto> moveMenuItem(
            @PathVariable Long menuId,
            @RequestParam(required = false) Integer newPosition,
            @RequestParam(required = false) Long newParentId) {
        log.info("Moving menu item {} to position {} under parent {}", menuId, newPosition, newParentId);
        MenuItemResponseDto menu = menuService.moveMenuItem(menuId, newPosition, newParentId);
        return ApiResponse.success("Menu item moved successfully", menu);
    }

    /**
     * Clean up permissions for deleted menus
     */
    @PostMapping("/cleanup")
    @PreAuthorize("hasAnyAuthority('DEVELOPER')")
    public ApiResponse<String> cleanupDeletedMenuPermissions() {
        log.info("Cleaning up permissions for deleted menus");
        menuService.cleanupDeletedMenuPermissions();
        return ApiResponse.success("Deleted menu permissions cleaned up successfully", "Cleanup completed");
    }
}