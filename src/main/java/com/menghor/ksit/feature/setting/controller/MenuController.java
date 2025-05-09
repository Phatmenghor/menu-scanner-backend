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
    public ApiResponse<UserMenuDto> getCurrentUserMenu() {
        UserEntity currentUser = securityUtils.getCurrentUser();
        log.info("Fetching menu for current user: {}", currentUser.getUsername());
        
        UserMenuDto userMenu = menuService.getCurrentUserMenu();
        return new ApiResponse<>("success", "User menu fetched successfully", userMenu);
    }
    
    @GetMapping("/user/{userId}")
    public ApiResponse<UserMenuDto> getUserMenu(@PathVariable Long userId) {
        log.info("Admin fetching menu for user with ID: {}", userId);
        
        UserMenuDto userMenu = menuService.getUserMenu(userId);
        return new ApiResponse<>("success", "User menu fetched successfully", userMenu);
    }

    @GetMapping("/all")
    public ApiResponse<List<MenuItemDto>> getAllMenuItems() {
        log.info("Fetching all menu items for admin");
        List<MenuItemDto> allMenus = menuService.getAllMenuItems();
        return new ApiResponse<>("success", "All menu items fetched successfully", allMenus);
    }

    @PostMapping
    public ApiResponse<MenuItemDto> createMenuItem(@RequestBody CreateMenuItemRequest request) {
        log.info("Creating new menu item with key: {}", request.getMenuKey());
        MenuItemDto createdItem = menuService.createMenuItem(request);
        return new ApiResponse<>("success", "Menu item created successfully", createdItem);
    }

    @PutMapping("/{id}")
    public ApiResponse<MenuItemDto> updateMenuItem(@PathVariable Long id, @RequestBody UpdateMenuItemRequest request) {
        log.info("Updating menu item with ID: {}", id);
        MenuItemDto updatedItem = menuService.updateMenuItem(id, request);
        return new ApiResponse<>("success", "Menu item updated successfully", updatedItem);
    }
    
    @PatchMapping("/reposition")
    public ApiResponse<MenuItemDto> repositionMenuItem(@RequestBody RepositionMenuItemRequest request) {
        log.info("Repositioning menu item: {}", request.getMenuKey());
        MenuItemDto updatedItem = menuService.repositionMenuItem(request);
        return new ApiResponse<>("success", "Menu item repositioned successfully", updatedItem);
    }

    @PatchMapping("/{menuKey}/toggle")
    public ApiResponse<MenuItemDto> toggleMenuItem(@PathVariable String menuKey, @RequestParam boolean enabled) {
        log.info("Toggling menu item {} to {}", menuKey, enabled ? "enabled" : "disabled");
        MenuItemDto updatedItem = menuService.toggleMenuItemStatus(menuKey, enabled);
        return new ApiResponse<>("success", "Menu item status updated successfully", updatedItem);
    }

    @DeleteMapping("/{menuKey}")
    public ApiResponse<Void> deleteMenuItem(@PathVariable String menuKey) {
        log.info("Deleting menu item with key: {}", menuKey);
        menuService.deleteMenuItem(menuKey);
        return new ApiResponse<>("success", "Menu item deleted successfully", null);
    }
    
    @GetMapping("/access/{userId}")
    public ApiResponse<List<String>> getUserMenuAccess(@PathVariable Long userId) {
        log.info("Fetching menu access for user with ID: {}", userId);
        List<String> accessibleMenus = menuService.getUserMenuAccess(userId);
        return new ApiResponse<>("success", "User menu access fetched successfully", accessibleMenus);
    }
    
    @PostMapping("/access")
    public ApiResponse<UserMenuAccessDto> updateUserMenuAccess(@RequestBody UpdateUserMenuAccessRequest request) {
        log.info("Updating menu access for user ID {} to menu {}: {}", 
                request.getUserId(), request.getMenuKey(), request.isHasAccess());
        
        UserMenuAccessDto result = menuService.updateUserMenuAccess(request);
        return new ApiResponse<>("success", "Menu access updated successfully", result);
    }
    
    @PostMapping("/access/bulk")
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