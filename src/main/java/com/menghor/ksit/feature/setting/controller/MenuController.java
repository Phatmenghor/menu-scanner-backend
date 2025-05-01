package com.menghor.ksit.feature.setting.controller;

import com.menghor.ksit.exceptoins.response.ApiResponse;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.setting.dto.request.*;
import com.menghor.ksit.feature.setting.dto.response.MenuItemDto;
import com.menghor.ksit.feature.setting.dto.response.UserMenuAccessDto;
import com.menghor.ksit.feature.setting.dto.response.UserMenuDto;
import com.menghor.ksit.feature.setting.dto.service.MenuService;
import com.menghor.ksit.utils.database.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/menu")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Menu", description = "Dashboard menu management API")
public class MenuController {

    private final MenuService menuService;
    private final SecurityUtils securityUtils;

    @GetMapping("/current")
    @Operation(summary = "Get current user's menu", description = "Returns the menu structure that the current user can access")
    public ApiResponse<UserMenuDto> getCurrentUserMenu() {
        UserEntity currentUser = securityUtils.getCurrentUser();
        log.info("Fetching menu for current user: {}", currentUser.getUsername());
        
        UserMenuDto userMenu = menuService.getCurrentUserMenu();
        return new ApiResponse<>("success", "User menu fetched successfully", userMenu);
    }
    
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER')")
    @Operation(summary = "Get a user's menu by user ID", description = "Admin only: Returns the menu structure for a specific user")
    public ApiResponse<UserMenuDto> getUserMenu(@PathVariable Long userId) {
        log.info("Admin fetching menu for user with ID: {}", userId);
        
        UserMenuDto userMenu = menuService.getUserMenu(userId);
        return new ApiResponse<>("success", "User menu fetched successfully", userMenu);
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER')")
    @Operation(summary = "Get all menu items", description = "Returns all menu items for admin management")
    public ApiResponse<List<MenuItemDto>> getAllMenuItems() {
        log.info("Fetching all menu items for admin");
        List<MenuItemDto> allMenus = menuService.getAllMenuItems();
        return new ApiResponse<>("success", "All menu items fetched successfully", allMenus);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER')")
    @Operation(summary = "Create menu item", description = "Creates a new menu item")
    public ApiResponse<MenuItemDto> createMenuItem(@RequestBody CreateMenuItemRequest request) {
        log.info("Creating new menu item with key: {}", request.getMenuKey());
        MenuItemDto createdItem = menuService.createMenuItem(request);
        return new ApiResponse<>("success", "Menu item created successfully", createdItem);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER')")
    @Operation(summary = "Update menu item", description = "Updates an existing menu item")
    public ApiResponse<MenuItemDto> updateMenuItem(@PathVariable Long id, @RequestBody UpdateMenuItemRequest request) {
        log.info("Updating menu item with ID: {}", id);
        MenuItemDto updatedItem = menuService.updateMenuItem(id, request);
        return new ApiResponse<>("success", "Menu item updated successfully", updatedItem);
    }
    
    @PatchMapping("/reposition")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER')")
    @Operation(summary = "Reposition menu item", description = "Changes the position of a menu item")
    public ApiResponse<MenuItemDto> repositionMenuItem(@RequestBody RepositionMenuItemRequest request) {
        log.info("Repositioning menu item: {}", request.getMenuKey());
        MenuItemDto updatedItem = menuService.repositionMenuItem(request);
        return new ApiResponse<>("success", "Menu item repositioned successfully", updatedItem);
    }

    @PatchMapping("/{menuKey}/toggle")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER')")
    @Operation(summary = "Toggle menu item status", description = "Enables or disables a menu item")
    public ApiResponse<MenuItemDto> toggleMenuItem(@PathVariable String menuKey, @RequestParam boolean enabled) {
        log.info("Toggling menu item {} to {}", menuKey, enabled ? "enabled" : "disabled");
        MenuItemDto updatedItem = menuService.toggleMenuItemStatus(menuKey, enabled);
        return new ApiResponse<>("success", "Menu item status updated successfully", updatedItem);
    }

    @DeleteMapping("/{menuKey}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER')")
    @Operation(summary = "Delete menu item", description = "Deletes a menu item")
    public ApiResponse<Void> deleteMenuItem(@PathVariable String menuKey) {
        log.info("Deleting menu item with key: {}", menuKey);
        menuService.deleteMenuItem(menuKey);
        return new ApiResponse<>("success", "Menu item deleted successfully", null);
    }
    
    @GetMapping("/access/{userId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER')")
    @Operation(summary = "Get user's menu access", description = "Returns list of menu keys that the user can access")
    public ApiResponse<List<String>> getUserMenuAccess(@PathVariable Long userId) {
        log.info("Fetching menu access for user with ID: {}", userId);
        List<String> accessibleMenus = menuService.getUserMenuAccess(userId);
        return new ApiResponse<>("success", "User menu access fetched successfully", accessibleMenus);
    }
    
    @PostMapping("/access")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER')")
    @Operation(summary = "Update user's access to a menu item", description = "Grants or revokes a user's access to a specific menu item")
    public ApiResponse<UserMenuAccessDto> updateUserMenuAccess(@RequestBody UpdateUserMenuAccessRequest request) {
        log.info("Updating menu access for user ID {} to menu {}: {}", 
                request.getUserId(), request.getMenuKey(), request.isHasAccess());
        
        UserMenuAccessDto result = menuService.updateUserMenuAccess(request);
        return new ApiResponse<>("success", "Menu access updated successfully", result);
    }
    
    @PostMapping("/access/bulk")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER')")
    @Operation(summary = "Bulk update user's menu access", description = "Grants or revokes a user's access to multiple menu items at once")
    public ApiResponse<Map<String, Object>> bulkUpdateUserMenuAccess(@RequestBody BulkMenuAccessRequest request) {
        log.info("Bulk updating menu access for user ID {}: {} menu items", 
                request.getUserId(), request.getMenuKeys().size());
        
        int updatedCount = menuService.bulkUpdateUserMenuAccess(request);
        
        Map<String, Object> result = Map.of(
            "userId", request.getUserId(),
            "updatedCount", updatedCount,
            "totalRequested", request.getMenuKeys().size()
        );
        
        return new ApiResponse<>("success", "Menu access bulk updated successfully", result);
    }
}