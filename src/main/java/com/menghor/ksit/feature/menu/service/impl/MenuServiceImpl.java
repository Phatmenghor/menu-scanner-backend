package com.menghor.ksit.feature.menu.service.impl;

import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.exceptoins.error.NotFoundException;
import com.menghor.ksit.feature.auth.models.Role;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.auth.repository.UserRepository;
import com.menghor.ksit.feature.menu.dto.request.*;
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

        // Auto-initialize user permissions if they don't exist
        initializeUserPermissionsIfNeeded(user);

        // Get all active menus
        List<MenuItemEntity> allMenus = menuItemRepository.findByStatusOrderByDisplayOrderAscIdAsc(Status.ACTIVE);

        // Get user's permissions (all menus will be returned, but with canView flags)
        Map<Long, MenuPermissionEntity> userPermissions = getUserCustomPermissions(userId);

        // Build response with permission status - ALL menus included
        List<UserMenuResponseDto> result = allMenus.stream()
                .map(menu -> buildUserMenuResponseWithPermission(menu, userPermissions))
                .collect(Collectors.toList());

        // Build hierarchical structure
        List<UserMenuResponseDto> hierarchicalResult = buildUserMenuHierarchy(result);

        log.info("Retrieved {} menus with permissions for user: {}", hierarchicalResult.size(), userId);
        return hierarchicalResult;
    }

    /**
     * Initialize user permissions for ALL menus based on their roles if they don't exist
     * Each user gets ALL menus, but canView is set based on their roles
     */
    @Transactional
    public void initializeUserPermissionsIfNeeded(UserEntity user) {
        // Check if user already has any permissions
        List<MenuPermissionEntity> existingPermissions = menuPermissionRepository
                .findByUserIdAndStatus(user.getId(), Status.ACTIVE);

        if (!existingPermissions.isEmpty()) {
            log.debug("User {} already has menu permissions", user.getId());
            return;
        }

        log.info("Initializing ALL menu permissions for user ID: {} with roles: {}",
                user.getId(), user.getRoles().stream().map(r -> r.getName()).collect(Collectors.toList()));

        // Get ALL active menus
        List<MenuItemEntity> allMenus = menuItemRepository.findByStatusOrderByDisplayOrderAscIdAsc(Status.ACTIVE);

        // Get user roles
        List<RoleEnum> userRoles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        // Create user-specific permissions for ALL menus
        List<MenuPermissionEntity> userPermissions = new ArrayList<>();

        for (MenuItemEntity menu : allMenus) {
            boolean canView = determineDefaultPermissionForMenu(menu.getCode(), userRoles);

            MenuPermissionEntity permission = new MenuPermissionEntity();
            permission.setUser(user);
            permission.setMenuItem(menu);
            permission.setCanView(canView);
            permission.setDisplayOrder(menu.getDisplayOrder());
            permission.setStatus(Status.ACTIVE);
            permission.setRole(null); // User-specific permission

            userPermissions.add(permission);
        }

        // Save all user permissions
        menuPermissionRepository.saveAll(userPermissions);

        log.info("Created {} menu permissions for user: {} (with {} viewable)",
                userPermissions.size(), user.getId(),
                userPermissions.stream().mapToInt(p -> p.getCanView() ? 1 : 0).sum());
    }

    /**
     * Determine default permission for a menu based on user roles
     * Based on your exact requirements:
     * - Admin + Developer: Full access (including Request)
     * - Staff + Teacher: Dashboard, Attendance, Survey, Student score, Schedule, Payment (NO Request)
     * - Student: Dashboard, Schedule, Payment only (NO Request, NO Attendance, NO Survey)
     */
    private boolean determineDefaultPermissionForMenu(String menuCode, List<RoleEnum> userRoles) {
        // Define menu permissions based on user type
        Set<RoleEnum> allowedRoles = getMenuAllowedRoles(menuCode);

        if (allowedRoles == null || allowedRoles.isEmpty()) {
            return false; // Default to no access if not defined
        }

        // Check if user has any of the allowed roles
        return userRoles.stream().anyMatch(allowedRoles::contains);
    }

    /**
     * Define which roles can access which menus
     * Based on your exact requirements:
     * - Admin + Developer: Full access (including Request)
     * - Staff + Teacher: Dashboard, Attendance, Survey, Student score, Schedule, Payment (NO Request)
     * - Student: Dashboard, Schedule, Payment only (NO Request, NO Attendance, NO Survey)
     */
    private Set<RoleEnum> getMenuAllowedRoles(String menuCode) {
        // Define role groups
        Set<RoleEnum> adminDeveloper = Set.of(RoleEnum.ADMIN, RoleEnum.DEVELOPER);
        Set<RoleEnum> staffTeacher = Set.of(RoleEnum.STAFF, RoleEnum.TEACHER);
        Set<RoleEnum> studentOnly = Set.of(RoleEnum.STUDENT);
        Set<RoleEnum> allStaffTypes = Set.of(RoleEnum.ADMIN, RoleEnum.DEVELOPER, RoleEnum.STAFF, RoleEnum.TEACHER);

        // Students get very limited access
        Set<RoleEnum> studentLimitedAccess = Set.of(RoleEnum.ADMIN, RoleEnum.DEVELOPER, RoleEnum.STAFF, RoleEnum.TEACHER, RoleEnum.STUDENT);
        Set<RoleEnum> staffTeacherAndAdmin = Set.of(RoleEnum.ADMIN, RoleEnum.DEVELOPER, RoleEnum.STAFF, RoleEnum.TEACHER);

        return switch (menuCode) {
            // Dashboard - accessible by all (Student: ✓, Staff/Teacher: ✓, Admin/Developer: ✓)
            case "DASHBOARD" -> studentLimitedAccess;

            // Master Data - Admin/Developer only (Student: ✗, Staff/Teacher: ✗, Admin/Developer: ✓)
            case "MASTER_DATA", "MANAGE_CLASS", "MANAGE_SEMESTER", "MANAGE_MAJOR",
                 "MANAGE_DEPARTMENT", "MANAGE_ROOM", "MANAGE_COURSE", "MANAGE_SUBJECT" -> adminDeveloper;

            // Users management - Admin/Developer only (Student: ✗, Staff/Teacher: ✗, Admin/Developer: ✓)
            case "USERS", "ADMIN", "STAFF_OFFICER", "TEACHERS" -> adminDeveloper;

            // Students management - Admin/Developer/Staff/Teacher (Student: ✗, Staff/Teacher: ✓, Admin/Developer: ✓)
            case "STUDENTS", "ADD_MULTIPLE_USERS", "ADD_SINGLE_USER", "STUDENTS_LIST" -> allStaffTypes;

            // Attendance - Staff/Teacher access only (Student: ✗, Staff/Teacher: ✓, Admin/Developer: ✗)
            case "ATTENDANCE", "CLASS_SCHEDULE", "HISTORY_RECORDS", "STUDENT_RECORDS" -> staffTeacher;

            // Survey - Staff/Teacher access only (Student: ✗, Staff/Teacher: ✓, Admin/Developer: ✗)
            case "SURVEY", "RESULT_LIST", "MANAGE_QA", "SURVEY_STUDENT_RECORDS" -> staffTeacher;

            // Score management - Staff/Teacher access only (Student: ✗, Staff/Teacher: ✓, Admin/Developer: ✗)
            case "SCORE_SUBMITTED", "SUBMITTED_LIST", "SCORE_SETTING", "STUDENT_SCORE" -> staffTeacher;

            // Schedule - All users can view (Student: ✓, Staff/Teacher: ✓, Admin/Developer: ✓)
            case "SCHEDULE" -> studentLimitedAccess;

            // Manage Schedule - Admin/Developer only (Student: ✗, Staff/Teacher: ✗, Admin/Developer: ✓)
            case "MANAGE_SCHEDULE" -> adminDeveloper;

            // Request - Admin/Developer only (Student: ✗, Staff/Teacher: ✗, Admin/Developer: ✓)
            case "REQUEST" -> adminDeveloper;

            // Payment - All users (Student: ✓, Staff/Teacher: ✓, Admin/Developer: ✓)
            case "PAYMENT" -> studentLimitedAccess;

            // Role Permission - Developer only (Student: ✗, Staff/Teacher: ✗, Admin/Developer: ✓)
            case "ROLE_PERMISSION" -> Set.of(RoleEnum.DEVELOPER);

            // Default - no access
            default -> Set.of();
        };
    }

    /**
     * Initialize menu permissions for all existing users when system starts
     */
    @Transactional
    public void initializeMenuPermissionsForAllExistingUsers() {
        log.info("Initializing menu permissions for all existing users...");

        List<UserEntity> allUsers = userRepository.findAll();
        int processedUsers = 0;

        for (UserEntity user : allUsers) {
            try {
                initializeUserPermissionsIfNeeded(user);
                processedUsers++;
            } catch (Exception e) {
                log.error("Error processing user {}: {}", user.getUsername(), e.getMessage());
            }
        }

        log.info("Processed {} existing users for menu permission initialization", processedUsers);
    }

    /**
     * Called when new user is created - automatically assign all menu permissions
     */
    @Transactional
    public void initializeMenuPermissionsForNewUser(Long userId) {
        log.info("Auto-initializing menu permissions for new user ID: {}", userId);

        UserEntity user = getUserById(userId);
        initializeUserPermissionsIfNeeded(user);
    }

    @Override
    @Transactional
    public List<UserMenuResponseDto> refreshUserMenuPermissionsAfterRoleChange(Long userId) {
        log.info("Refreshing menu permissions for user {} after role change", userId);

        UserEntity user = getUserById(userId);

        // Delete existing user permissions to reset them
        List<MenuPermissionEntity> existingPermissions = menuPermissionRepository
                .findByUserIdAndStatus(userId, Status.ACTIVE);

        for (MenuPermissionEntity permission : existingPermissions) {
            permission.setStatus(Status.DELETED);
        }

        // Re-initialize with new role-based permissions
        initializeUserPermissionsIfNeeded(user);

        return getAllMenusWithPermissions(userId);
    }

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

        List<UserMenuResponseDto> result = allMenus.stream()
                .map(menu -> {
                    UserMenuResponseDto dto = menuMapper.toUserMenuResponse(menu);
                    boolean canView = determineDefaultPermissionForMenu(menu.getCode(), List.of(role));
                    dto.setCanView(canView);
                    dto.setIsCustomized(false);
                    return dto;
                })
                .collect(Collectors.toList());

        return buildUserMenuHierarchy(result);
    }

    @Override
    @Transactional
    public List<UserMenuResponseDto> updateUserMenuPermissions(Long userId, UserMenuUpdateDto updateDto) {
        log.info("Updating menu permissions for user ID: {}", userId);

        UserEntity user = getUserById(userId);

        // Process each menu permission update
        for (MenuPermissionUpdateDto permissionDto : updateDto.getMenuPermissions()) {
            MenuItemEntity menuItem = getMenuItemById(permissionDto.getMenuId());

            // Find existing user permission
            Optional<MenuPermissionEntity> existingPermissionOpt = menuPermissionRepository
                    .findByUserIdAndMenuItemIdAndStatus(userId, permissionDto.getMenuId(), Status.ACTIVE);

            if (existingPermissionOpt.isPresent()) {
                // Update existing permission
                MenuPermissionEntity permission = existingPermissionOpt.get();
                permission.setCanView(permissionDto.getCanView() != null ? permissionDto.getCanView() : false);
                menuPermissionRepository.save(permission);
                log.debug("Updated permission for user {} menu {}: canView={}",
                        userId, menuItem.getCode(), permission.getCanView());
            } else {
                // This shouldn't happen if user was properly initialized, but create if missing
                MenuPermissionEntity permission = new MenuPermissionEntity();
                permission.setUser(user);
                permission.setMenuItem(menuItem);
                permission.setCanView(permissionDto.getCanView() != null ? permissionDto.getCanView() : false);
                permission.setDisplayOrder(menuItem.getDisplayOrder());
                permission.setStatus(Status.ACTIVE);
                permission.setRole(null);

                menuPermissionRepository.save(permission);
                log.debug("Created new permission for user {} menu {}: canView={}",
                        userId, menuItem.getCode(), permission.getCanView());
            }
        }

        log.info("Updated {} menu permissions for user: {}", updateDto.getMenuPermissions().size(), userId);
        return getAllMenusWithPermissions(userId);
    }

    @Override
    public List<UserMenuResponseDto> getUserViewableMenus(Long userId) {
        log.info("Fetching viewable menus for user ID: {}", userId);

        List<UserMenuResponseDto> allMenusWithPermissions = getAllMenusWithPermissions(userId);
        List<UserMenuResponseDto> viewableMenus = filterViewableMenus(allMenusWithPermissions);

        log.info("User {} has {} viewable menus out of {} total",
                userId, viewableMenus.size(), allMenusWithPermissions.size());
        return viewableMenus;
    }

    @Override
    @Transactional
    public List<UserMenuResponseDto> resetUserMenusToDefault(Long userId) {
        log.info("Resetting user menu permissions to defaults for user ID: {}", userId);

        UserEntity user = getUserById(userId);

        // Soft delete all user-specific permissions
        List<MenuPermissionEntity> userPermissions = menuPermissionRepository
                .findByUserIdAndStatus(userId, Status.ACTIVE);

        userPermissions.forEach(p -> p.setStatus(Status.DELETED));

        // Re-initialize with role-based defaults
        initializeUserPermissionsIfNeeded(user);

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

    private UserMenuResponseDto buildUserMenuResponseWithPermission(MenuItemEntity menu,
                                                                    Map<Long, MenuPermissionEntity> userPermissions) {
        UserMenuResponseDto dto = menuMapper.toUserMenuResponse(menu);

        // Get user's permission for this menu
        MenuPermissionEntity userPermission = userPermissions.get(menu.getId());
        if (userPermission != null) {
            dto.setCanView(userPermission.getCanView());
            dto.setIsCustomized(true);
        } else {
            // This shouldn't happen if user was properly initialized
            dto.setCanView(false);
            dto.setIsCustomized(false);
        }

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

    // ===== NEW MENU MANAGEMENT METHODS =====

    @Override
    @Transactional
    public MenuItemResponseDto createMenuItem(MenuCreateDto createDto) {
        log.info("Creating new menu item with code: {}", createDto.getCode());

        // Check if menu code already exists
        if (menuItemRepository.existsByCodeAndStatus(createDto.getCode(), Status.ACTIVE)) {
            throw new RuntimeException("Menu code '" + createDto.getCode() + "' already exists");
        }

        MenuItemEntity menuItem = new MenuItemEntity();
        menuItem.setCode(createDto.getCode());
        menuItem.setTitle(createDto.getTitle());
        menuItem.setRoute(createDto.getRoute());
        menuItem.setIcon(createDto.getIcon());
        menuItem.setIsParent(createDto.getIsParent() != null ? createDto.getIsParent() : false);
        menuItem.setStatus(Status.ACTIVE);

        // Set parent if provided
        if (createDto.getParentId() != null) {
            MenuItemEntity parent = getMenuItemById(createDto.getParentId());
            menuItem.setParent(parent);
        }

        // Set display order
        if (createDto.getDisplayOrder() != null) {
            menuItem.setDisplayOrder(createDto.getDisplayOrder());
        } else {
            // Auto-assign display order
            Integer maxOrder = createDto.getParentId() != null
                    ? menuItemRepository.getMaxDisplayOrderForChildren(createDto.getParentId(), Status.ACTIVE)
                    : menuItemRepository.getMaxDisplayOrderForParents(Status.ACTIVE);
            menuItem.setDisplayOrder((maxOrder != null ? maxOrder : 0) + 1);
        }

        MenuItemEntity savedMenu = menuItemRepository.save(menuItem);

        // Initialize permissions for all existing users
        initializeNewMenuForAllUsers(savedMenu);

        log.info("Created new menu item: {} with ID: {}", createDto.getCode(), savedMenu.getId());
        return menuMapper.toMenuItemResponse(savedMenu);
    }

    @Override
    @Transactional
    public MenuItemResponseDto updateMenuItem(Long menuId, MenuUpdateDto updateDto) {
        log.info("Updating menu item with ID: {}", menuId);

        MenuItemEntity menuItem = getMenuItemById(menuId);

        // Update fields if provided
        if (updateDto.getTitle() != null) menuItem.setTitle(updateDto.getTitle());
        if (updateDto.getRoute() != null) menuItem.setRoute(updateDto.getRoute());
        if (updateDto.getIcon() != null) menuItem.setIcon(updateDto.getIcon());
        if (updateDto.getIsParent() != null) menuItem.setIsParent(updateDto.getIsParent());
        if (updateDto.getDisplayOrder() != null) menuItem.setDisplayOrder(updateDto.getDisplayOrder());

        // Update parent if provided
        if (updateDto.getParentId() != null) {
            MenuItemEntity parent = getMenuItemById(updateDto.getParentId());
            menuItem.setParent(parent);
        }

        MenuItemEntity updatedMenu = menuItemRepository.save(menuItem);
        log.info("Updated menu item: {}", menuItem.getCode());

        return menuMapper.toMenuItemResponse(updatedMenu);
    }

    @Override
    @Transactional
    public MenuItemResponseDto deleteMenuItem(Long menuId) {
        log.info("Soft deleting menu item with ID: {}", menuId);

        MenuItemEntity menuItem = getMenuItemById(menuId);
        menuItem.setStatus(Status.DELETED);

        // Soft delete all child menus
        List<MenuItemEntity> children = menuItemRepository.findChildMenusByParentIdAndStatus(menuId, Status.ACTIVE);
        for (MenuItemEntity child : children) {
            child.setStatus(Status.DELETED);
        }

        menuItemRepository.save(menuItem);
        if (!children.isEmpty()) {
            menuItemRepository.saveAll(children);
        }

        // Handle user permissions for deleted menu
        handleDeletedMenuPermissions(menuId);
        for (MenuItemEntity child : children) {
            handleDeletedMenuPermissions(child.getId());
        }

        log.info("Soft deleted menu item: {} and {} children", menuItem.getCode(), children.size());
        return menuMapper.toMenuItemResponse(menuItem);
    }

    @Override
    @Transactional
    public List<MenuItemResponseDto> reorderMenuItems(MenuBatchReorderDto reorderDto) {
        log.info("Reordering {} menu items", reorderDto.getMenuReorders().size());

        for (var reorder : reorderDto.getMenuReorders()) {
            MenuItemEntity menuItem = getMenuItemById(reorder.getMenuId());
            menuItem.setDisplayOrder(reorder.getNewDisplayOrder());
            menuItemRepository.save(menuItem);
        }

        log.info("Successfully reordered menu items");
        return getAllMenuItems();
    }

    @Override
    @Transactional
    public MenuItemResponseDto moveMenuItem(Long menuId, Integer newPosition, Long newParentId) {
        log.info("Moving menu item {} to position {} under parent {}", menuId, newPosition, newParentId);

        MenuItemEntity menuItem = getMenuItemById(menuId);

        // Update parent if provided
        if (newParentId != null) {
            MenuItemEntity newParent = getMenuItemById(newParentId);
            menuItem.setParent(newParent);
        }

        // Update position
        if (newPosition != null) {
            menuItem.setDisplayOrder(newPosition);
        }

        MenuItemEntity savedMenu = menuItemRepository.save(menuItem);
        log.info("Moved menu item: {}", menuItem.getCode());

        return menuMapper.toMenuItemResponse(savedMenu);
    }

    @Override
    @Transactional
    public void cleanupDeletedMenuPermissions() {
        log.info("Cleaning up permissions for deleted menus");

        // This method can be called periodically to clean up permissions
        // for menus that have been soft deleted
        List<MenuItemEntity> deletedMenus = menuItemRepository.findByStatusOrderByDisplayOrderAscIdAsc(Status.DELETED);

        for (MenuItemEntity deletedMenu : deletedMenus) {
            List<MenuPermissionEntity> permissions = menuPermissionRepository
                    .findByMenuItemIdAndStatus(deletedMenu.getId(), Status.ACTIVE);

            for (MenuPermissionEntity permission : permissions) {
                permission.setStatus(Status.DELETED);
            }

            if (!permissions.isEmpty()) {
                menuPermissionRepository.saveAll(permissions);
            }
        }

        log.info("Cleaned up permissions for {} deleted menus", deletedMenus.size());
    }

    // ===== HELPER METHODS FOR NEW FUNCTIONALITY =====

    /**
     * Initialize permissions for a new menu item for all existing users
     */
    @Transactional
    public void initializeNewMenuForAllUsers(MenuItemEntity newMenu) {
        log.info("Initializing permissions for new menu '{}' for all users", newMenu.getCode());

        List<UserEntity> allUsers = userRepository.findAll();
        List<MenuPermissionEntity> newPermissions = new ArrayList<>();

        for (UserEntity user : allUsers) {
            List<RoleEnum> userRoles = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toList());

            boolean canView = determineDefaultPermissionForMenu(newMenu.getCode(), userRoles);

            MenuPermissionEntity permission = new MenuPermissionEntity();
            permission.setUser(user);
            permission.setMenuItem(newMenu);
            permission.setCanView(canView);
            permission.setDisplayOrder(newMenu.getDisplayOrder());
            permission.setStatus(Status.ACTIVE);
            permission.setRole(null); // User-specific permission

            newPermissions.add(permission);
        }

        menuPermissionRepository.saveAll(newPermissions);
        log.info("Created {} permissions for new menu '{}'", newPermissions.size(), newMenu.getCode());
    }

    /**
     * Handle permissions when a menu is deleted
     */
    @Transactional
    public void handleDeletedMenuPermissions(Long menuId) {
        log.info("Handling permissions for deleted menu ID: {}", menuId);

        List<MenuPermissionEntity> permissions = menuPermissionRepository
                .findByMenuItemIdAndStatus(menuId, Status.ACTIVE);

        for (MenuPermissionEntity permission : permissions) {
            permission.setStatus(Status.DELETED);
        }

        if (!permissions.isEmpty()) {
            menuPermissionRepository.saveAll(permissions);
            log.info("Soft deleted {} permissions for menu ID: {}", permissions.size(), menuId);
        }
    }
}