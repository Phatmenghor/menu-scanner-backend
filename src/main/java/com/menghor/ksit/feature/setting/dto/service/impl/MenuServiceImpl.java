package com.menghor.ksit.feature.setting.dto.service.impl;

import com.menghor.ksit.enumations.PositionType;
import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.exceptoins.error.BadRequestException;
import com.menghor.ksit.exceptoins.error.DuplicateNameException;
import com.menghor.ksit.exceptoins.error.NotFoundException;
import com.menghor.ksit.feature.auth.models.Role;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.auth.repository.UserRepository;
import com.menghor.ksit.feature.setting.dto.request.*;
import com.menghor.ksit.feature.setting.dto.response.MenuItemDto;
import com.menghor.ksit.feature.setting.dto.response.UserMenuAccessDto;
import com.menghor.ksit.feature.setting.dto.response.UserMenuDto;
import com.menghor.ksit.feature.setting.dto.service.MenuService;
import com.menghor.ksit.feature.setting.mapper.MenuMapper;
import com.menghor.ksit.feature.setting.models.MenuItem;
import com.menghor.ksit.feature.setting.models.UserMenuAccess;
import com.menghor.ksit.feature.setting.repository.MenuItemRepository;
import com.menghor.ksit.feature.setting.repository.UserMenuAccessRepository;
import com.menghor.ksit.utils.database.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuServiceImpl implements MenuService {

    private final MenuItemRepository menuItemRepository;
    private final UserMenuAccessRepository userMenuAccessRepository;
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;
    private final MenuMapper menuMapper;

    @Override
    public UserMenuDto getUserMenu(Long userId) {
        log.info("Fetching menu for user with ID: {}", userId);

        // Verify user exists
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + userId));

        // Get all parent menus the user can access
        List<MenuItem> parentMenus = userMenuAccessRepository.findAccessibleParentMenusByUserId(userId);

        // Get all children for each parent menu
        Map<String, List<MenuItem>> childrenMap = new HashMap<>();
        for (MenuItem parentMenu : parentMenus) {
            List<MenuItem> children = userMenuAccessRepository.findAccessibleChildMenusByUserIdAndParent(
                    userId, parentMenu.getMenuKey());
            childrenMap.put(parentMenu.getMenuKey(), children);
        }

        // Use mapper to build the menu DTO
        UserMenuDto userMenu = menuMapper.toUserMenuDto(parentMenus, childrenMap);

        log.info("Found {} menu items for user", userMenu.getMenuItems().size());
        return userMenu;
    }

    @Override
    public UserMenuDto getCurrentUserMenu() {
        UserEntity currentUser = securityUtils.getCurrentUser();
        return getUserMenu(currentUser.getId());
    }

    @Override
    @Transactional
    public MenuItemDto createMenuItem(CreateMenuItemRequest createRequest) {
        log.info("Creating menu item with key: {}", createRequest.getMenuKey());

        // Check if menu key already exists
        if (menuItemRepository.existsByMenuKey(createRequest.getMenuKey())) {
            throw new DuplicateNameException("Menu item with key " + createRequest.getMenuKey() + " already exists");
        }

        // If it's a child menu, verify parent exists
        if (createRequest.getParentKey() != null && !createRequest.getParentKey().isEmpty()) {
            MenuItem parent = menuItemRepository.findByMenuKey(createRequest.getParentKey())
                    .orElseThrow(() -> new NotFoundException("Parent menu item not found: " + createRequest.getParentKey()));

            if (!parent.isParent()) {
                throw new BadRequestException("Specified parent is not a parent menu item: " + createRequest.getParentKey());
            }
        }

        MenuItem menuItem = new MenuItem();
        menuItem.setMenuKey(createRequest.getMenuKey());
        menuItem.setMenuName(createRequest.getMenuName());
        menuItem.setParentKey(createRequest.getParentKey());
        menuItem.setParent(createRequest.isParent());
        menuItem.setIcon(createRequest.getIcon());
        menuItem.setRoute(createRequest.getRoute());
        menuItem.setEnabled(true);

        // Handle positioning based on position type
        setMenuItemPosition(menuItem, createRequest.getPositionType(), createRequest.getDisplayOrder(), null);

        MenuItem savedMenuItem = menuItemRepository.save(menuItem);

        // Give admin and developer users access to the new menu item by default
        giveAdminAccessToMenuItem(savedMenuItem);

        return menuMapper.toDto(savedMenuItem);
    }

    /**
     * Calculate and set the display order for a menu item based on positioning type
     */
    private void setMenuItemPosition(MenuItem menuItem, PositionType positionType, Integer displayOrder, String positionAfterItem) {
        String parentKey = menuItem.getParentKey();
        List<MenuItem> siblings;

        // Get sibling menu items in the same level
        if (parentKey == null || parentKey.isEmpty()) {
            siblings = menuItemRepository.findByParentKeyIsNullOrderByDisplayOrderAsc();
        } else {
            siblings = menuItemRepository.findByParentKeyOrderByDisplayOrderAsc(parentKey);
        }

        switch (positionType) {
            case TOP:
                // Find minimum display order and place this item before it
                menuItem.setDisplayOrder(siblings.isEmpty() ? 1 :
                        siblings.stream()
                                .mapToInt(MenuItem::getDisplayOrder)
                                .min()
                                .orElse(1) - 1);
                break;

            case BOTTOM:
                // Find maximum display order and place this item after it
                menuItem.setDisplayOrder(siblings.isEmpty() ? 1 :
                        siblings.stream()
                                .mapToInt(MenuItem::getDisplayOrder)
                                .max()
                                .orElse(0) + 1);
                break;

            case AFTER_ITEM:
                if (positionAfterItem != null && !positionAfterItem.isEmpty()) {
                    // Find the reference item
                    MenuItem referenceItem = menuItemRepository.findByMenuKey(positionAfterItem)
                            .orElseThrow(() -> new NotFoundException("Reference menu item not found: " + positionAfterItem));

                    // Check if the reference item is at the same level
                    if ((parentKey == null && referenceItem.getParentKey() != null) ||
                            (parentKey != null && !parentKey.equals(referenceItem.getParentKey()))) {
                        throw new BadRequestException("Reference menu item is not at the same level");
                    }

                    // Set display order to be after the reference item
                    int referenceOrder = referenceItem.getDisplayOrder();

                    // Find all items with display order > referenceOrder and increment them
                    siblings.stream()
                            .filter(s -> s.getDisplayOrder() > referenceOrder)
                            .forEach(s -> {
                                s.setDisplayOrder(s.getDisplayOrder() + 1);
                                menuItemRepository.save(s);
                            });

                    menuItem.setDisplayOrder(referenceOrder + 1);
                } else {
                    throw new BadRequestException("Position after item key is required for AFTER_ITEM position type");
                }
                break;

            case SPECIFIC:
            default:
                // Use the provided display order or set to bottom if not provided
                if (displayOrder != null) {
                    menuItem.setDisplayOrder(displayOrder);
                } else {
                    // Default to bottom if no specific order provided
                    menuItem.setDisplayOrder(siblings.isEmpty() ? 1 :
                            siblings.stream()
                                    .mapToInt(MenuItem::getDisplayOrder)
                                    .max()
                                    .orElse(0) + 1);
                }
                break;
        }
    }

    /**
     * Give admin and developer users access to a menu item
     */
    private void giveAdminAccessToMenuItem(MenuItem menuItem) {
        List<UserEntity> users = userRepository.findAll();

        for (UserEntity user : users) {
            // Get user roles
            Set<RoleEnum> userRoles = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toSet());

            // Admin and Developer users get access by default
            if (userRoles.contains(RoleEnum.ADMIN) || userRoles.contains(RoleEnum.DEVELOPER)) {
                UserMenuAccess access = new UserMenuAccess();
                access.setUser(user);
                access.setMenuItem(menuItem);
                access.setHasAccess(true);
                userMenuAccessRepository.save(access);
            }
        }
    }

    @Override
    @Transactional
    public MenuItemDto updateMenuItem(Long id, UpdateMenuItemRequest updateRequest) {
        log.info("Updating menu item with ID: {}", id);

        MenuItem existingMenu = menuItemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Menu item not found with ID: " + id));

        // Check if the key is being changed and if it conflicts
        if (!existingMenu.getMenuKey().equals(updateRequest.getMenuKey())) {
            if (menuItemRepository.existsByMenuKey(updateRequest.getMenuKey())) {
                throw new DuplicateNameException("Menu item with key " + updateRequest.getMenuKey() + " already exists");
            }
        }

        // If parent key is changing, verify new parent exists
        if (updateRequest.getParentKey() != null && !updateRequest.getParentKey().equals(existingMenu.getParentKey())) {
            menuItemRepository.findByMenuKey(updateRequest.getParentKey())
                    .orElseThrow(() -> new NotFoundException("Parent menu item not found: " + updateRequest.getParentKey()));
        }

        existingMenu.setMenuKey(updateRequest.getMenuKey());
        existingMenu.setMenuName(updateRequest.getMenuName());
        existingMenu.setParentKey(updateRequest.getParentKey());
        existingMenu.setParent(updateRequest.isParent());
        existingMenu.setIcon(updateRequest.getIcon());
        existingMenu.setRoute(updateRequest.getRoute());
        existingMenu.setEnabled(updateRequest.isEnabled());

        // Handle positioning if requested
        if (updateRequest.getPositionType() != PositionType.SPECIFIC ||
                !Objects.equals(existingMenu.getDisplayOrder(), updateRequest.getDisplayOrder())) {
            setMenuItemPosition(
                    existingMenu,
                    updateRequest.getPositionType(),
                    updateRequest.getDisplayOrder(),
                    updateRequest.getPositionAfterItem()
            );
        }

        MenuItem updatedMenu = menuItemRepository.save(existingMenu);
        return menuMapper.toDto(updatedMenu);
    }

    @Override
    @Transactional
    public MenuItemDto repositionMenuItem(RepositionMenuItemRequest request) {
        log.info("Repositioning menu item with key: {}", request.getMenuKey());

        MenuItem menuItem = menuItemRepository.findByMenuKey(request.getMenuKey())
                .orElseThrow(() -> new NotFoundException("Menu item not found with key: " + request.getMenuKey()));

        // Handle positioning based on position type
        setMenuItemPosition(
                menuItem,
                request.getPositionType(),
                request.getDisplayOrder(),
                request.getPositionAfterItem()
        );

        MenuItem updatedMenu = menuItemRepository.save(menuItem);
        return menuMapper.toDto(updatedMenu);
    }

    @Override
    @Transactional
    public MenuItemDto toggleMenuItemStatus(String menuKey, boolean enabled) {
        log.info("Toggling menu item {} to {}", menuKey, enabled ? "enabled" : "disabled");

        MenuItem menuItem = menuItemRepository.findByMenuKey(menuKey)
                .orElseThrow(() -> new NotFoundException("Menu item not found with key: " + menuKey));

        menuItem.setEnabled(enabled);
        MenuItem updatedMenu = menuItemRepository.save(menuItem);
        return menuMapper.toDto(updatedMenu);
    }

    @Override
    public List<MenuItemDto> getAllMenuItems() {
        log.info("Fetching all menu items");

        List<MenuItem> allMenus = menuItemRepository.findAll();

        // Use mapper to build hierarchy
        return menuMapper.toDtoHierarchy(allMenus);
    }

    @Override
    @Transactional
    public void deleteMenuItem(String menuKey) {
        log.info("Deleting menu item with key: {}", menuKey);

        MenuItem menuItem = menuItemRepository.findByMenuKey(menuKey)
                .orElseThrow(() -> new NotFoundException("Menu item not found with key: " + menuKey));

        // Check if it has children
        List<MenuItem> children = menuItemRepository.findByParentKeyAndEnabledTrueOrderByDisplayOrderAsc(menuKey);
        if (!children.isEmpty()) {
            throw new BadRequestException("Cannot delete menu item with children. Delete children first.");
        }

        // Delete all user access entries for this menu item
        List<UserMenuAccess> accessEntries = userMenuAccessRepository.findAll().stream()
                .filter(uma -> uma.getMenuItem().getMenuKey().equals(menuKey))
                .collect(Collectors.toList());

        userMenuAccessRepository.deleteAll(accessEntries);

        // Delete the menu item
        menuItemRepository.delete(menuItem);
        log.info("Menu item deleted: {}", menuKey);
    }

    @Override
    @Transactional
    public UserMenuAccessDto updateUserMenuAccess(UpdateUserMenuAccessRequest request) {
        log.info("Updating user menu access: {} - {} - {}",
                request.getUserId(), request.getMenuKey(), request.isHasAccess());

        UserEntity user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + request.getUserId()));

        MenuItem menuItem = menuItemRepository.findByMenuKey(request.getMenuKey())
                .orElseThrow(() -> new NotFoundException("Menu item not found with key: " + request.getMenuKey()));

        // Check if access record already exists
        UserMenuAccess userMenuAccess = userMenuAccessRepository.findByUserAndMenuItem(user, menuItem)
                .orElseGet(() -> {
                    // Create new access record if it doesn't exist
                    UserMenuAccess newAccess = new UserMenuAccess();
                    newAccess.setUser(user);
                    newAccess.setMenuItem(menuItem);
                    return newAccess;
                });

        userMenuAccess.setHasAccess(request.isHasAccess());
        userMenuAccessRepository.save(userMenuAccess);

        // Use mapper for conversion
        return menuMapper.toDto(userMenuAccess);
    }

    @Override
    @Transactional
    public int bulkUpdateUserMenuAccess(BulkMenuAccessRequest request) {
        log.info("Updating menu access for user {} - {} menu items - access: {}",
                request.getUserId(), request.getMenuKeys().size(), request.isHasAccess());

        UserEntity user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + request.getUserId()));

        int updatedCount = 0;

        for (String menuKey : request.getMenuKeys()) {
            try {
                MenuItem menuItem = menuItemRepository.findByMenuKey(menuKey)
                        .orElseThrow(() -> new NotFoundException("Menu item not found with key: " + menuKey));

                // Check if access record already exists
                UserMenuAccess userMenuAccess = userMenuAccessRepository.findByUserAndMenuItem(user, menuItem)
                        .orElseGet(() -> {
                            // Create new access record if it doesn't exist
                            UserMenuAccess newAccess = new UserMenuAccess();
                            newAccess.setUser(user);
                            newAccess.setMenuItem(menuItem);
                            return newAccess;
                        });

                userMenuAccess.setHasAccess(request.isHasAccess());
                userMenuAccessRepository.save(userMenuAccess);
                updatedCount++;
            } catch (Exception e) {
                log.error("Error updating access for menu key: {}", menuKey, e);
            }
        }

        log.info("Updated access for {} menu items", updatedCount);
        return updatedCount;
    }

    @Override
    public List<String> getUserMenuAccess(Long userId) {
        log.info("Getting menu access for user with ID: {}", userId);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + userId));

        List<UserMenuAccess> accessList = userMenuAccessRepository.findByUser(user);

        return accessList.stream()
                .filter(UserMenuAccess::isHasAccess)
                .map(access -> access.getMenuItem().getMenuKey())
                .collect(Collectors.toList());
    }

    @Override
    @EventListener(ContextRefreshedEvent.class)
    @Transactional
    public void initializeDefaultMenuItems() {
        log.info("Checking for default menu items...");

        // Skip if menu items already exist
        if (menuItemRepository.count() > 0) {
            log.info("Menu items already exist, skipping initialization");
            return;
        }

        log.info("Initializing default menu items");

        // Create default menu structure
        createDefaultMenuItems();

        // Give access to default users based on roles
        setupDefaultUserAccess();

        log.info("Default menu items created successfully");
    }

    /**
     * Create default menu items and their hierarchy
     */
    private void createDefaultMenuItems() {
        // 1. Dashboard
        createDefaultMenuItem("dashboard", "Dashboard", null, false, "dashboard", "/dashboard", 1, true);

        // 2. Master Data
        createDefaultMenuItem("master-data", "Master Data", null, true, "database", null, 2, true);
        createDefaultMenuItem("manage-classes", "Manage Classes", "master-data", false, "layers", "/master/classes", 1, true);
        createDefaultMenuItem("manage-semesters", "Manage Semesters", "master-data", false, "calendar", "/master/semesters", 2, true);
        createDefaultMenuItem("manage-majors", "Manage Majors", "master-data", false, "book", "/master/majors", 3, true);
        createDefaultMenuItem("manage-departments", "Manage Departments", "master-data", false, "briefcase", "/master/departments", 4, true);
        createDefaultMenuItem("manage-rooms", "Manage Rooms", "master-data", false, "home", "/master/rooms", 5, true);

        // 3. Users
        createDefaultMenuItem("users", "Users", null, true, "users", null, 3, true);
        createDefaultMenuItem("members", "Members", "users", false, "user", "/users/members", 1, true);
        createDefaultMenuItem("teachers", "Teachers", "users", false, "user-check", "/users/teachers", 2, true);

        // 4. Students
        createDefaultMenuItem("students", "Students", null, true, "users", null, 4, true);
        createDefaultMenuItem("add-multiple-students", "Add Multiple Students", "students", false, "user-plus", "/students/add-multiple", 1, true);
        createDefaultMenuItem("add-single-student", "Add Single Student", "students", false, "user-plus", "/students/add", 2, true);
        createDefaultMenuItem("student-list", "Student List", "students", false, "list", "/students/list", 3, true);
        createDefaultMenuItem("dropout-students", "Dropout Students", "students", false, "user-minus", "/students/dropout", 4, true);

        // 5. Schedule
        createDefaultMenuItem("schedule", "Schedule", null, false, "calendar", "/schedule", 5, true);

        // 6. Courses
        createDefaultMenuItem("courses", "Courses", null, false, "book-open", "/courses", 6, true);

        // 7. Student Scores
        createDefaultMenuItem("student-scores", "Student Scores", null, false, "award", "/scores", 7, true);

        // 8. Score Submissions
        createDefaultMenuItem("score-submissions", "Score Submissions", null, false, "clipboard", "/submissions", 8, true);

        // 9. Requests
        createDefaultMenuItem("requests", "Requests", null, false, "message-square", "/requests", 9, true);

        // 10. Theme Settings
        createDefaultMenuItem("theme-settings", "Theme Settings", null, false, "settings", "/settings/theme", 10, true);
    }

    /**
     * Helper method to create a default menu item
     */
    private MenuItem createDefaultMenuItem(
            String key,
            String name,
            String parentKey,
            boolean isParent,
            String icon,
            String route,
            int order,
            boolean enabled) {

        MenuItem menuItem = new MenuItem();
        menuItem.setMenuKey(key);
        menuItem.setMenuName(name);
        menuItem.setParentKey(parentKey);
        menuItem.setParent(isParent);
        menuItem.setIcon(icon);
        menuItem.setRoute(route);
        menuItem.setDisplayOrder(order);
        menuItem.setEnabled(enabled);

        return menuItemRepository.save(menuItem);
    }

    /**
     * Setup default user access based on roles
     */
    private void setupDefaultUserAccess() {
        log.info("Setting up default user access");

        // Get all users
        List<UserEntity> users = userRepository.findAll();

        // Get all menu items
        List<MenuItem> allMenuItems = menuItemRepository.findAll();

        for (UserEntity user : users) {
            // Get user roles
            Set<RoleEnum> userRoles = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toSet());

            for (MenuItem menuItem : allMenuItems) {
                boolean hasAccess = false;

                // Logic to determine access based on roles and menu item
                if (userRoles.contains(RoleEnum.DEVELOPER) || userRoles.contains(RoleEnum.ADMIN)) {
                    // Admins and developers get access to everything
                    hasAccess = true;
                } else if (userRoles.contains(RoleEnum.STAFF)) {
                    // Staff get access to most things except admin-only items
                    String key = menuItem.getMenuKey();

                    if (key.equals("users") ||
                            key.equals("members") ||
                            key.equals("teachers") ||
                            key.equals("add-multiple-students") ||
                            key.equals("theme-settings")) {
                        hasAccess = false;
                    } else {
                        hasAccess = true;
                    }
                } else if (userRoles.contains(RoleEnum.STUDENT)) {
                    // Students get limited access
                    String key = menuItem.getMenuKey();

                    if (key.equals("dashboard") ||
                            key.equals("schedule") ||
                            key.equals("courses") ||
                            key.equals("student-scores") ||
                            key.equals("requests")) {
                        hasAccess = true;
                    } else {
                        hasAccess = false;
                    }
                }

                // Create access record
                UserMenuAccess userMenuAccess = new UserMenuAccess();
                userMenuAccess.setUser(user);
                userMenuAccess.setMenuItem(menuItem);
                userMenuAccess.setHasAccess(hasAccess);
                userMenuAccessRepository.save(userMenuAccess);
            }
        }

        log.info("Default user access setup completed");
    }
}