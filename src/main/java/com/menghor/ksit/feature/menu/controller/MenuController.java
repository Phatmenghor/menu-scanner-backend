package com.menghor.ksit.feature.menu.controller;

import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.exceptoins.response.ApiResponse;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.menu.dto.response.MenuItemResponseDto;
import com.menghor.ksit.feature.menu.dto.response.UserMenuResponseDto;
import com.menghor.ksit.feature.menu.dto.resquest.MenuReorderDto;
import com.menghor.ksit.feature.menu.dto.resquest.UserMenuPermissionCreateDto;
import com.menghor.ksit.feature.menu.dto.resquest.UserMenuReorderDto;
import com.menghor.ksit.feature.menu.dto.update.UserMenuPermissionUpdateDto;
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
     * Get current user's menu permissions
     */
    @GetMapping("/my-menus")
    public ApiResponse<List<UserMenuResponseDto>> getMyMenus() {
        log.info("Getting current user's menus");
        UserEntity currentUser = securityUtils.getCurrentUser();
        List<UserMenuResponseDto> menus = menuService.getUserMenus(currentUser.getId());
        return ApiResponse.success("User menus retrieved successfully", menus);
    }

    /**
     * Get user menus by user ID (Admin/Staff only)
     */
    @GetMapping("/users/{userId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER', 'STAFF')")
    public ApiResponse<List<UserMenuResponseDto>> getUserMenus(@PathVariable Long userId) {
        log.info("Getting menus for user ID: {}", userId);
        List<UserMenuResponseDto> menus = menuService.getUserMenus(userId);
        return ApiResponse.success("User menus retrieved successfully", menus);
    }

    /**
     * Get menus by role (Admin/Developer only)
     */
    @GetMapping("/roles/{role}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER')")
    public ApiResponse<List<UserMenuResponseDto>> getMenusByRole(@PathVariable RoleEnum role) {
        log.info("Getting menus for role: {}", role);
        List<UserMenuResponseDto> menus = menuService.getMenusByRole(role);
        return ApiResponse.success("Role menus retrieved successfully", menus);
    }

    /**
     * Get all menu items (Admin/Developer only)
     */
    @GetMapping("/all")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER')")
    public ApiResponse<List<MenuItemResponseDto>> getAllMenuItems() {
        log.info("Getting all menu items");
        List<MenuItemResponseDto> menus = menuService.getAllMenuItems();
        return ApiResponse.success("All menu items retrieved successfully", menus);
    }

    /**
     * Add custom menu permission for user (Admin/Developer only)
     */
    @PostMapping("/permissions")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER')")
    public ApiResponse<UserMenuResponseDto> addUserMenuPermission(@Valid @RequestBody UserMenuPermissionCreateDto createDto) {
        log.info("Adding menu permission for user ID: {} and menu ID: {}", 
                createDto.getUserId(), createDto.getMenuItemId());
        UserMenuResponseDto permission = menuService.addUserMenuPermission(createDto);
        return ApiResponse.success("Menu permission added successfully", permission);
    }

    /**
     * Update user menu permission (Admin/Developer only)
     */
    @PutMapping("/permissions/{permissionId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER')")
    public ApiResponse<UserMenuResponseDto> updateUserMenuPermission(
            @PathVariable Long permissionId,
            @Valid @RequestBody UserMenuPermissionUpdateDto updateDto) {
        log.info("Updating menu permission ID: {}", permissionId);
        UserMenuResponseDto permission = menuService.updateUserMenuPermission(permissionId, updateDto);
        return ApiResponse.success("Menu permission updated successfully", permission);
    }

    /**
     * Remove user menu permission (Admin/Developer only)
     */
    @DeleteMapping("/permissions/users/{userId}/menus/{menuItemId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER')")
    public ApiResponse<Void> removeUserMenuPermission(@PathVariable Long userId, @PathVariable Long menuItemId) {
        log.info("Removing menu permission for user ID: {} and menu ID: {}", userId, menuItemId);
        menuService.removeUserMenuPermission(userId, menuItemId);
        return ApiResponse.success("Menu permission removed successfully", null);
    }

    /**
     * Reorder user menus (Admin/Developer only)
     */
    @PutMapping("/reorder/users")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER')")
    public ApiResponse<List<UserMenuResponseDto>> reorderUserMenus(@Valid @RequestBody UserMenuReorderDto reorderDto) {
        log.info("Reordering menus for user ID: {}", reorderDto.getUserId());
        List<UserMenuResponseDto> menus = menuService.reorderUserMenus(reorderDto);
        return ApiResponse.success("User menus reordered successfully", menus);
    }

    /**
     * Reorder current user's menus
     */
    @PutMapping("/reorder/my-menus")
    public ApiResponse<List<UserMenuResponseDto>> reorderMyMenus(@Valid @RequestBody UserMenuReorderDto reorderDto) {
        UserEntity currentUser = securityUtils.getCurrentUser();
        reorderDto.setUserId(currentUser.getId());
        log.info("Reordering menus for current user ID: {}", currentUser.getId());
        List<UserMenuResponseDto> menus = menuService.reorderUserMenus(reorderDto);
        return ApiResponse.success("Your menus reordered successfully", menus);
    }

    /**
     * Reorder default menu items (Developer only)
     */
    @PutMapping("/reorder/default")
    @PreAuthorize("hasAuthority('DEVELOPER')")
    public ApiResponse<List<MenuItemResponseDto>> reorderDefaultMenus(@Valid @RequestBody MenuReorderDto reorderDto) {
        log.info("Reordering default menu items");
        List<MenuItemResponseDto> menus = menuService.reorderDefaultMenus(reorderDto);
        return ApiResponse.success("Default menus reordered successfully", menus);
    }

    /**
     * Initialize menu permissions for a user (Admin/Developer only)
     */
    @PostMapping("/initialize/{userId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER')")
    public ApiResponse<Void> initializeUserMenuPermissions(@PathVariable Long userId) {
        log.info("Initializing menu permissions for user ID: {}", userId);
        menuService.initializeUserMenuPermissions(userId);
        return ApiResponse.success("Menu permissions initialized successfully", null);
    }
}