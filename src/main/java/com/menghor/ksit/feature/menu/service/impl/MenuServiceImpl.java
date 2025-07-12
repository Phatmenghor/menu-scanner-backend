package com.menghor.ksit.feature.menu.service.impl;

import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.exceptoins.error.BadRequestException;
import com.menghor.ksit.exceptoins.error.NotFoundException;
import com.menghor.ksit.feature.auth.models.Role;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.auth.repository.UserRepository;
import com.menghor.ksit.feature.menu.dto.mapper.MenuMapper;
import com.menghor.ksit.feature.menu.dto.response.MenuItemResponseDto;
import com.menghor.ksit.feature.menu.dto.response.UserMenuResponseDto;
import com.menghor.ksit.feature.menu.dto.resquest.MenuReorderDto;
import com.menghor.ksit.feature.menu.dto.resquest.UserMenuPermissionCreateDto;
import com.menghor.ksit.feature.menu.dto.resquest.UserMenuReorderDto;
import com.menghor.ksit.feature.menu.service.MenuService;
import com.menghor.ksit.feature.menu.dto.update.UserMenuPermissionUpdateDto;
import com.menghor.ksit.feature.menu.models.MenuItemEntity;
import com.menghor.ksit.feature.menu.models.MenuPermissionEntity;
import com.menghor.ksit.feature.menu.models.UserMenuPermissionEntity;
import com.menghor.ksit.feature.menu.repository.MenuItemRepository;
import com.menghor.ksit.feature.menu.repository.MenuPermissionRepository;
import com.menghor.ksit.feature.menu.repository.UserMenuPermissionRepository;
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
    private final UserMenuPermissionRepository userMenuPermissionRepository;
    private final UserRepository userRepository;
    private final MenuMapper menuMapper;

    @Override
    public List<UserMenuResponseDto> getUserMenus(Long userId) {
        log.info("Getting menu items for user ID: {}", userId);
        
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + userId));

        // Get user's roles
        List<RoleEnum> userRoles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        // Get user's custom menu permissions
        List<UserMenuPermissionEntity> userPermissions = userMenuPermissionRepository
                .findViewableMenusByUser(userId, Status.ACTIVE);

        // Get default role-based permissions
        List<MenuPermissionEntity> rolePermissions = menuPermissionRepository
                .findViewableMenusByRoles(userRoles, Status.ACTIVE);

        // Merge and build menu structure
        return buildUserMenuStructure(userPermissions, rolePermissions, userId);
    }

    @Override
    public List<UserMenuResponseDto> getMenusByRole(RoleEnum role) {
        log.info("Getting menu items for role: {}", role);
        
        List<MenuPermissionEntity> rolePermissions = menuPermissionRepository
                .findViewableMenusByRole(role, Status.ACTIVE);

        return buildRoleMenuStructure(rolePermissions);
    }

    @Override
    public List<MenuItemResponseDto> getAllMenuItems() {
        log.info("Getting all menu items");
        
        List<MenuItemEntity> topLevelMenus = menuItemRepository
                .findTopLevelMenus(Status.ACTIVE);

        return menuMapper.toMenuItemResponseDtoList(topLevelMenus);
    }

    @Override
    @Transactional
    public UserMenuResponseDto addUserMenuPermission(UserMenuPermissionCreateDto createDto) {
        log.info("Adding user menu permission for user ID: {} and menu item ID: {}", 
                createDto.getUserId(), createDto.getMenuItemId());

        // Validate user exists
        UserEntity user = userRepository.findById(createDto.getUserId())
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + createDto.getUserId()));

        // Validate menu item exists
        MenuItemEntity menuItem = menuItemRepository.findById(createDto.getMenuItemId())
                .orElseThrow(() -> new NotFoundException("Menu item not found with ID: " + createDto.getMenuItemId()));

        // Check if permission already exists
        if (userMenuPermissionRepository.existsByUserIdAndMenuItemId(createDto.getUserId(), createDto.getMenuItemId())) {
            throw new BadRequestException("User menu permission already exists for this user and menu item");
        }

        UserMenuPermissionEntity userPermission = new UserMenuPermissionEntity();
        userPermission.setUser(user);
        userPermission.setMenuItem(menuItem);
        userPermission.setCanView(createDto.getCanView());
        userPermission.setDisplayOrder(createDto.getDisplayOrder());
        userPermission.setCustomTitle(createDto.getCustomTitle());
        userPermission.setCustomIcon(createDto.getCustomIcon());
        userPermission.setStatus(createDto.getStatus());

        UserMenuPermissionEntity savedPermission = userMenuPermissionRepository.save(userPermission);
        
        return menuMapper.toUserMenuResponseDto(savedPermission);
    }

    @Override
    @Transactional
    public UserMenuResponseDto updateUserMenuPermission(Long userMenuPermissionId, UserMenuPermissionUpdateDto updateDto) {
        log.info("Updating user menu permission with ID: {}", userMenuPermissionId);

        UserMenuPermissionEntity permission = userMenuPermissionRepository.findById(userMenuPermissionId)
                .orElseThrow(() -> new NotFoundException("User menu permission not found with ID: " + userMenuPermissionId));

        // Update only provided fields
        if (updateDto.getCanView() != null) {
            permission.setCanView(updateDto.getCanView());
        }
        if (updateDto.getDisplayOrder() != null) {
            permission.setDisplayOrder(updateDto.getDisplayOrder());
        }
        if (updateDto.getCustomTitle() != null) {
            permission.setCustomTitle(updateDto.getCustomTitle());
        }
        if (updateDto.getCustomIcon() != null) {
            permission.setCustomIcon(updateDto.getCustomIcon());
        }
        if (updateDto.getStatus() != null) {
            permission.setStatus(updateDto.getStatus());
        }

        UserMenuPermissionEntity updatedPermission = userMenuPermissionRepository.save(permission);
        
        return menuMapper.toUserMenuResponseDto(updatedPermission);
    }

    @Override
    @Transactional
    public void removeUserMenuPermission(Long userId, Long menuItemId) {
        log.info("Removing user menu permission for user ID: {} and menu item ID: {}", userId, menuItemId);
        
        userMenuPermissionRepository.deleteByUserIdAndMenuItemId(userId, menuItemId);
    }

    @Override
    @Transactional
    public List<UserMenuResponseDto> reorderUserMenus(UserMenuReorderDto reorderDto) {
        log.info("Reordering menus for user ID: {}", reorderDto.getUserId());

        // Validate user exists
        UserEntity user = userRepository.findById(reorderDto.getUserId())
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + reorderDto.getUserId()));

        // Update or create user menu permissions with new order
        for (UserMenuReorderDto.UserMenuOrderItem orderItem : reorderDto.getMenuOrders()) {
            Optional<UserMenuPermissionEntity> existingPermission = userMenuPermissionRepository
                    .findByUserIdAndMenuItemId(reorderDto.getUserId(), orderItem.getMenuItemId());

            if (existingPermission.isPresent()) {
                // Update existing permission
                UserMenuPermissionEntity permission = existingPermission.get();
                permission.setDisplayOrder(orderItem.getDisplayOrder());
                permission.setCanView(orderItem.getCanView());
                userMenuPermissionRepository.save(permission);
            } else {
                // Create new permission
                MenuItemEntity menuItem = menuItemRepository.findById(orderItem.getMenuItemId())
                        .orElseThrow(() -> new NotFoundException("Menu item not found with ID: " + orderItem.getMenuItemId()));

                UserMenuPermissionEntity newPermission = new UserMenuPermissionEntity();
                newPermission.setUser(user);
                newPermission.setMenuItem(menuItem);
                newPermission.setDisplayOrder(orderItem.getDisplayOrder());
                newPermission.setCanView(orderItem.getCanView());
                newPermission.setStatus(Status.ACTIVE);
                userMenuPermissionRepository.save(newPermission);
            }
        }

        // Return updated user menus
        return getUserMenus(reorderDto.getUserId());
    }

    @Override
    @Transactional
    public List<MenuItemResponseDto> reorderDefaultMenus(MenuReorderDto reorderDto) {
        log.info("Reordering default menu items");

        for (MenuReorderDto.MenuOrderItem orderItem : reorderDto.getMenuOrders()) {
            MenuItemEntity menuItem = menuItemRepository.findById(orderItem.getMenuItemId())
                    .orElseThrow(() -> new NotFoundException("Menu item not found with ID: " + orderItem.getMenuItemId()));

            menuItem.setDisplayOrder(orderItem.getDisplayOrder());
            menuItemRepository.save(menuItem);
        }

        return getAllMenuItems();
    }

    @Override
    @Transactional
    public void initializeUserMenuPermissions(Long userId) {
        log.info("Initializing menu permissions for user ID: {}", userId);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + userId));

        // Get user's roles
        List<RoleEnum> userRoles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        // Get all menu permissions for user's roles
        List<MenuPermissionEntity> rolePermissions = menuPermissionRepository
                .findViewableMenusByRoles(userRoles, Status.ACTIVE);

        // Create user menu permissions based on role permissions
        for (MenuPermissionEntity rolePermission : rolePermissions) {
            // Check if user permission already exists
            if (!userMenuPermissionRepository.existsByUserIdAndMenuItemId(userId, rolePermission.getMenuItem().getId())) {
                UserMenuPermissionEntity userPermission = new UserMenuPermissionEntity();
                userPermission.setUser(user);
                userPermission.setMenuItem(rolePermission.getMenuItem());
                userPermission.setCanView(rolePermission.getCanView());
                userPermission.setDisplayOrder(rolePermission.getDisplayOrder());
                userPermission.setStatus(Status.ACTIVE);
                
                userMenuPermissionRepository.save(userPermission);
            }
        }
    }

    // Helper methods
    private List<UserMenuResponseDto> buildUserMenuStructure(List<UserMenuPermissionEntity> userPermissions,
                                                            List<MenuPermissionEntity> rolePermissions, Long userId) {
        // Create map for quick lookup
        Map<Long, UserMenuPermissionEntity> userPermMap = userPermissions.stream()
                .collect(Collectors.toMap(p -> p.getMenuItem().getId(), p -> p));

        // Get all available menus from role permissions
        Set<MenuItemEntity> availableMenus = rolePermissions.stream()
                .map(MenuPermissionEntity::getMenuItem)
                .collect(Collectors.toSet());

        // Build final menu list with user customizations
        List<UserMenuResponseDto> menus = new ArrayList<>();
        
        for (MenuItemEntity menuItem : availableMenus) {
            if (menuItem.getParent() == null) { // Top level only
                UserMenuPermissionEntity userPerm = userPermMap.get(menuItem.getId());
                UserMenuResponseDto menuDto = buildUserMenuDto(menuItem, userPerm, userPermMap);
                menus.add(menuDto);
            }
        }

        // Sort by display order
        menus.sort(Comparator.comparing(UserMenuResponseDto::getDisplayOrder));
        return menus;
    }

    private List<UserMenuResponseDto> buildRoleMenuStructure(List<MenuPermissionEntity> rolePermissions) {
        // Get unique menu items
        Set<MenuItemEntity> menuItems = rolePermissions.stream()
                .map(MenuPermissionEntity::getMenuItem)
                .collect(Collectors.toSet());

        List<UserMenuResponseDto> menus = new ArrayList<>();
        
        for (MenuItemEntity menuItem : menuItems) {
            if (menuItem.getParent() == null) { // Top level only
                UserMenuResponseDto menuDto = menuMapper.toUserMenuResponseDtoFromMenuItem(menuItem);
                menus.add(menuDto);
            }
        }

        menus.sort(Comparator.comparing(UserMenuResponseDto::getDisplayOrder));
        return menus;
    }

    private UserMenuResponseDto buildUserMenuDto(MenuItemEntity menuItem, UserMenuPermissionEntity userPerm,
                                               Map<Long, UserMenuPermissionEntity> userPermMap) {
        UserMenuResponseDto dto = new UserMenuResponseDto();
        dto.setId(menuItem.getId());
        dto.setCode(menuItem.getCode());
        dto.setRoute(menuItem.getRoute());
        dto.setIsParent(menuItem.getIsParent());
        dto.setIsCustomized(userPerm != null);

        // Use custom values if available, otherwise use defaults
        if (userPerm != null) {
            dto.setTitle(userPerm.getCustomTitle() != null ? userPerm.getCustomTitle() : menuItem.getTitle());
            dto.setIcon(userPerm.getCustomIcon() != null ? userPerm.getCustomIcon() : menuItem.getIcon());
            dto.setDisplayOrder(userPerm.getDisplayOrder());
            dto.setCanView(userPerm.getCanView());
            dto.setCustomTitle(userPerm.getCustomTitle());
            dto.setCustomIcon(userPerm.getCustomIcon());
        } else {
            dto.setTitle(menuItem.getTitle());
            dto.setIcon(menuItem.getIcon());
            dto.setDisplayOrder(menuItem.getDisplayOrder());
            dto.setCanView(true);
        }

        // Build children recursively
        List<UserMenuResponseDto> children = new ArrayList<>();
        for (MenuItemEntity child : menuItem.getChildren()) {
            if (child.getStatus() == Status.ACTIVE) {
                UserMenuPermissionEntity childUserPerm = userPermMap.get(child.getId());
                UserMenuResponseDto childDto = buildUserMenuDto(child, childUserPerm, userPermMap);
                children.add(childDto);
            }
        }
        children.sort(Comparator.comparing(UserMenuResponseDto::getDisplayOrder));
        dto.setChildren(children);

        return dto;
    }
}