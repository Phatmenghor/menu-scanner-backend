package com.menghor.ksit.feature.menu.service.impl;

import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.exceptoins.error.NotFoundException;
import com.menghor.ksit.feature.auth.models.Role;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.auth.repository.UserRepository;
import com.menghor.ksit.feature.menu.dto.request.MenuPermissionUpdateDto;
import com.menghor.ksit.feature.menu.dto.request.UserMenuUpdateDto;
import com.menghor.ksit.feature.menu.dto.response.MenuItemResponseDto;
import com.menghor.ksit.feature.menu.dto.response.UserMenuResponseDto;
import com.menghor.ksit.feature.menu.mapper.MenuMapper;
import com.menghor.ksit.feature.menu.models.MenuItemEntity;
import com.menghor.ksit.feature.menu.models.MenuPermissionEntity;
import com.menghor.ksit.feature.menu.repository.MenuItemRepository;
import com.menghor.ksit.feature.menu.repository.MenuPermissionRepository;
import com.menghor.ksit.feature.menu.service.MenuService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuServiceImpl implements MenuService {

    private final MenuItemRepository menuItemRepository;
    private final MenuPermissionRepository menuPermissionRepository;
    private final UserRepository userRepository;
    private final MenuMapper menuMapper;

    @Override
    public List<UserMenuResponseDto> getAllMenusWithPermissions(Long userId) {
        log.info("Fetching ALL menus with permission status for user ID: {}", userId);

        UserEntity user = getUserById(userId);
        List<RoleEnum> userRoles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        // Get all active menus
        List<MenuItemEntity> allMenus = menuItemRepository.findByStatusOrderByDisplayOrderAscIdAsc(Status.ACTIVE);

        // Get user's custom permissions
        Map<Long, MenuPermissionEntity> userPermissions = getUserCustomPermissions(userId);

        // Get role-based permissions for fallback
        Map<Long, Boolean> rolePermissions = getRoleBasedPermissions(userRoles);

        // Build response with permission status
        List<UserMenuResponseDto> result = allMenus.stream()
                .map(menu -> buildUserMenuResponse(menu, userPermissions, rolePermissions))
                .collect(Collectors.toList());

        // Build hierarchical structure
        List<UserMenuResponseDto> hierarchicalResult = buildUserMenuHierarchy(result);

        log.info("Retrieved {} menus with permissions for user: {}", hierarchicalResult.size(), userId);
        return hierarchicalResult;
    }

    /**
     * NEW METHOD: Initialize menu permissions for a new user
     * This is called when a new user is created
     */
    @Transactional
    public void initializeMenuPermissionsForNewUser(Long userId) {
        log.info("Initializing menu permissions for new user ID: {}", userId);

        UserEntity user = getUserById(userId);
        List<RoleEnum> userRoles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        // Check if user already has any permissions (avoid duplicate initialization)
        List<MenuPermissionEntity> existingPermissions = menuPermissionRepository
                .findByUserIdAndStatus(userId, Status.ACTIVE);

        if (!existingPermissions.isEmpty()) {
            log.info("User {} already has menu permissions, skipping initialization", userId);
            return;
        }

        // User will use role-based permissions by default
        // No need to create user-specific permissions - they'll inherit from roles
        log.info("User {} will inherit menu permissions from roles: {}", userId, userRoles);
    }

    /**
     * NEW METHOD: Bulk initialize menu permissions for all existing users
     * This is called when the system starts up
     */
    @Transactional
    public void initializeMenuPermissionsForAllExistingUsers() {
        log.info("Initializing menu permissions for all existing users...");

        List<UserEntity> allUsers = userRepository.findAll();
        int processedUsers = 0;

        for (UserEntity user : allUsers) {
            try {
                // Just ensure the user can access the system - they'll inherit role permissions
                List<RoleEnum> userRoles = user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toList());

                log.debug("User {} has roles: {} - will inherit role-based menu permissions",
                        user.getUsername(), userRoles);

                processedUsers++;
            } catch (Exception e) {
                log.error("Error processing user {}: {}", user.getUsername(), e.getMessage());
            }
        }

        log.info("Processed {} existing users for menu permission inheritance", processedUsers);
    }

    /**
     * NEW METHOD: Update user roles and refresh menu permissions
     * Call this when user roles change
     */
    @Transactional
    public List<UserMenuResponseDto> refreshUserMenuPermissionsAfterRoleChange(Long userId) {
        log.info("Refreshing menu permissions for user {} after role change", userId);

        // User will automatically inherit new role permissions
        // No need to delete existing user permissions - the hierarchy handles it

        return getAllMenusWithPermissions(userId);
    }

    // Rest of the existing methods remain the same...

    @Override
    public List<MenuItemResponseDto> getAllMenuItems() {
        log.info("Fetching all menu items structure");

        List<MenuItemEntity> allMenus = menuItemRepository.findByStatusOrderByDisplayOrderAscIdAsc(Status.ACTIVE);
        List<MenuItemResponseDto> menuDtos = menuMapper.toMenuItemResponseList(allMenus);

        List<MenuItemResponseDto> result = buildMenuHierarchy(menuDtos);

        log.info("Retrieved {} top-level menu items", result.size());
        return result;
    }

    @Override
    public List<UserMenuResponseDto> getMenusByRole(RoleEnum role) {
        log.info("Fetching menus for role: {}", role);

        List<MenuItemEntity> allMenus = menuItemRepository.findByStatusOrderByDisplayOrderAscIdAsc(Status.ACTIVE);
        Map<Long, Boolean> rolePermissions = getRoleBasedPermissions(List.of(role));

        List<UserMenuResponseDto> result = allMenus.stream()
                .map(menu -> buildUserMenuResponseFromRole(menu, rolePermissions))
                .collect(Collectors.toList());

        return buildUserMenuHierarchy(result);
    }

    @Override
    @Transactional
    public List<UserMenuResponseDto> updateUserMenuPermissions(Long userId, UserMenuUpdateDto updateDto) {
        log.info("Updating menu permissions for user ID: {}", userId);

        UserEntity user = getUserById(userId);

        // Get existing user permissions
        List<MenuPermissionEntity> existingPermissions = menuPermissionRepository
                .findByUserIdAndStatus(userId, Status.ACTIVE);

        Map<Long, MenuPermissionEntity> existingPermissionsMap = existingPermissions.stream()
                .collect(Collectors.toMap(p -> p.getMenuItem().getId(), p -> p));

        Set<Long> menusToKeep = new HashSet<>();

        // Process each menu permission update
        for (MenuPermissionUpdateDto permissionDto : updateDto.getMenuPermissions()) {
            MenuItemEntity menuItem = getMenuItemById(permissionDto.getMenuId());

            MenuPermissionEntity permission = existingPermissionsMap.get(permissionDto.getMenuId());

            if (permission != null) {
                // Update existing permission
                permission.setCanView(permissionDto.getCanView());
                permission.setStatus(Status.ACTIVE);
                menusToKeep.add(permissionDto.getMenuId());
            } else {
                // Create new user-specific permission
                permission = new MenuPermissionEntity();
                permission.setUser(user);
                permission.setMenuItem(menuItem);
                permission.setCanView(permissionDto.getCanView());
                permission.setDisplayOrder(menuItem.getDisplayOrder());
                permission.setStatus(Status.ACTIVE);
                permission.setRole(null); // This is user-specific, not role-based

                menuPermissionRepository.save(permission);
                menusToKeep.add(permissionDto.getMenuId());
            }
        }

        // Soft delete permissions not in the update
        existingPermissions.stream()
                .filter(p -> !menusToKeep.contains(p.getMenuItem().getId()))
                .forEach(p -> p.setStatus(Status.DELETED));

        log.info("Updated menu permissions for user: {}", userId);
        return getAllMenusWithPermissions(userId);
    }

    @Override
    public List<UserMenuResponseDto> getUserViewableMenus(Long userId) {
        log.info("Fetching viewable menus for user ID: {}", userId);

        List<UserMenuResponseDto> allMenusWithPermissions = getAllMenusWithPermissions(userId);
        List<UserMenuResponseDto> viewableMenus = filterViewableMenus(allMenusWithPermissions);

        log.info("User {} has {} viewable menus", userId, viewableMenus.size());
        return viewableMenus;
    }

    @Override
    @Transactional
    public List<UserMenuResponseDto> resetUserMenusToDefault(Long userId) {
        log.info("Resetting user menu permissions to defaults for user ID: {}", userId);

        // Soft delete all user-specific permissions
        List<MenuPermissionEntity> userPermissions = menuPermissionRepository
                .findByUserIdAndStatus(userId, Status.ACTIVE);

        userPermissions.forEach(p -> p.setStatus(Status.DELETED));

        log.info("Reset {} custom menu permissions for user: {}", userPermissions.size(), userId);
        return getAllMenusWithPermissions(userId);
    }

    // Helper methods
    private UserEntity getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + userId));
    }

    private MenuItemEntity getMenuItemById(Long menuId) {
        return menuItemRepository.findById(menuId)
                .orElseThrow(() -> new NotFoundException("Menu item not found with ID: " + menuId));
    }

    private Map<Long, MenuPermissionEntity> getUserCustomPermissions(Long userId) {
        List<MenuPermissionEntity> userPermissions = menuPermissionRepository
                .findByUserIdAndStatus(userId, Status.ACTIVE);

        return userPermissions.stream()
                .collect(Collectors.toMap(p -> p.getMenuItem().getId(), p -> p));
    }

    private Map<Long, Boolean> getRoleBasedPermissions(List<RoleEnum> roles) {
        List<MenuPermissionEntity> rolePermissions = menuPermissionRepository
                .findViewableMenusByRoles(roles, Status.ACTIVE);

        return rolePermissions.stream()
                .filter(MenuPermissionEntity::getCanView)
                .collect(Collectors.toMap(
                        p -> p.getMenuItem().getId(),
                        menuPermissionEntity -> true,
                        (existing, replacement) -> existing
                ));
    }

    private UserMenuResponseDto buildUserMenuResponse(MenuItemEntity menu,
                                                      Map<Long, MenuPermissionEntity> userPermissions,
                                                      Map<Long, Boolean> rolePermissions) {
        UserMenuResponseDto dto = menuMapper.toUserMenuResponse(menu);

        // Check user-specific permission first
        MenuPermissionEntity userPermission = userPermissions.get(menu.getId());
        if (userPermission != null) {
            dto.setCanView(userPermission.getCanView());
            dto.setIsCustomized(true);
        } else {
            // Fall back to role-based permission
            dto.setCanView(rolePermissions.getOrDefault(menu.getId(), false));
            dto.setIsCustomized(false);
        }

        return dto;
    }

    private UserMenuResponseDto buildUserMenuResponseFromRole(MenuItemEntity menu,
                                                              Map<Long, Boolean> rolePermissions) {
        UserMenuResponseDto dto = menuMapper.toUserMenuResponse(menu);
        dto.setCanView(rolePermissions.getOrDefault(menu.getId(), false));
        dto.setIsCustomized(false);
        return dto;
    }

    private List<MenuItemResponseDto> buildMenuHierarchy(List<MenuItemResponseDto> flatMenus) {
        Map<Long, MenuItemResponseDto> menuMap = flatMenus.stream()
                .collect(Collectors.toMap(MenuItemResponseDto::getId, m -> m));

        List<MenuItemResponseDto> rootMenus = new ArrayList<>();

        for (MenuItemResponseDto menu : flatMenus) {
            if (menu.getParentId() == null) {
                rootMenus.add(menu);
            } else {
                MenuItemResponseDto parent = menuMap.get(menu.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(menu);
                }
            }
        }

        return rootMenus;
    }

    private List<UserMenuResponseDto> buildUserMenuHierarchy(List<UserMenuResponseDto> flatMenus) {
        Map<Long, UserMenuResponseDto> menuMap = flatMenus.stream()
                .collect(Collectors.toMap(UserMenuResponseDto::getId, m -> m));

        List<UserMenuResponseDto> rootMenus = new ArrayList<>();

        for (UserMenuResponseDto menu : flatMenus) {
            if (menu.getParentId() == null) {
                rootMenus.add(menu);
            } else {
                UserMenuResponseDto parent = menuMap.get(menu.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(menu);
                }
            }
        }

        return rootMenus;
    }

    private List<UserMenuResponseDto> filterViewableMenus(List<UserMenuResponseDto> menus) {
        return menus.stream()
                .filter(menu -> {
                    if (!menu.getCanView()) {
                        return false;
                    }

                    if (menu.getChildren() != null && !menu.getChildren().isEmpty()) {
                        List<UserMenuResponseDto> viewableChildren = filterViewableMenus(menu.getChildren());
                        menu.setChildren(viewableChildren);
                    }

                    return true;
                })
                .collect(Collectors.toList());
    }
}
