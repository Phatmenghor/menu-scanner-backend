package com.menghor.ksit.feature.menu.controller;

import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.exceptoins.response.ApiResponse;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.menu.dto.request.UserMenuUpdateDto;
import com.menghor.ksit.feature.menu.dto.response.MenuItemResponseDto;
import com.menghor.ksit.feature.menu.dto.response.UserMenuResponseDto;
import com.menghor.ksit.feature.menu.service.MenuService;
import com.menghor.ksit.utils.database.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
     * Get user menus by user ID
     */
    @GetMapping("/users/{userId}")
    public ApiResponse<List<UserMenuResponseDto>> getUserMenus(@PathVariable Long userId) {
        log.info("Getting all menus with permissions for user ID: {}", userId);
        List<UserMenuResponseDto> menus = menuService.getAllMenusWithPermissions(userId);
        return ApiResponse.success("User menus retrieved successfully", menus);
    }

    /**
     * Get menus by role
     */
    @GetMapping("/roles/{role}")
    public ApiResponse<List<UserMenuResponseDto>> getMenusByRole(@PathVariable RoleEnum role) {
        log.info("Getting menus for role: {}", role);
        List<UserMenuResponseDto> menus = menuService.getMenusByRole(role);
        return ApiResponse.success("Role menus retrieved successfully", menus);
    }

    /**
     * Get all menu items structure
     */
    @GetMapping("/all")
    public ApiResponse<List<MenuItemResponseDto>> getAllMenuItems() {
        log.info("Getting all menu items structure");
        List<MenuItemResponseDto> menus = menuService.getAllMenuItems();
        return ApiResponse.success("All menu items retrieved successfully", menus);
    }

    /**
     * Update user menu permissions - survey-like pattern
     */
    @PutMapping("/users/{userId}/permissions")
    public ApiResponse<List<UserMenuResponseDto>> updateUserMenuPermissions(
            @PathVariable Long userId,
            @Valid @RequestBody UserMenuUpdateDto updateDto) {
        log.info("Updating menu permissions for user ID: {}", userId);
        List<UserMenuResponseDto> menus = menuService.updateUserMenuPermissions(userId, updateDto);
        return ApiResponse.success("Menu permissions updated successfully", menus);
    }

    /**
     * Update current user's menu preferences
     */
    @PutMapping("/my-menus/permissions")
    public ApiResponse<List<UserMenuResponseDto>> updateMyMenuPermissions(@Valid @RequestBody UserMenuUpdateDto updateDto) {
        UserEntity currentUser = securityUtils.getCurrentUser();
        log.info("Updating menu permissions for current user ID: {}", currentUser.getId());
        List<UserMenuResponseDto> menus = menuService.updateUserMenuPermissions(currentUser.getId(), updateDto);
        return ApiResponse.success("Your menu permissions updated successfully", menus);
    }

    /**
     * Reset user's menu permissions to role defaults
     */
    @PostMapping("/users/{userId}/reset")
    public ApiResponse<List<UserMenuResponseDto>> resetUserMenusToDefault(@PathVariable Long userId) {
        log.info("Resetting menu permissions to defaults for user ID: {}", userId);
        List<UserMenuResponseDto> menus = menuService.resetUserMenusToDefault(userId);
        return ApiResponse.success("Menu permissions reset to defaults successfully", menus);
    }

    /**
     * Reset current user's menu permissions to role defaults
     */
    @PostMapping("/my-menus/reset")
    public ApiResponse<List<UserMenuResponseDto>> resetMyMenusToDefault() {
        UserEntity currentUser = securityUtils.getCurrentUser();
        log.info("Resetting menu permissions to defaults for current user ID: {}", currentUser.getId());
        List<UserMenuResponseDto> menus = menuService.resetUserMenusToDefault(currentUser.getId());
        return ApiResponse.success("Your menu permissions reset to defaults successfully", menus);
    }
}