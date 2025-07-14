package com.menghor.ksit.config;

import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.feature.menu.models.MenuItemEntity;
import com.menghor.ksit.feature.menu.models.MenuPermissionEntity;
import com.menghor.ksit.feature.menu.repository.MenuItemRepository;
import com.menghor.ksit.feature.menu.repository.MenuPermissionRepository;
import com.menghor.ksit.feature.menu.service.MenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(3) // Run after roles and users are initialized, but before data migration
public class DefaultMenuInitializer implements CommandLineRunner {

    private final MenuItemRepository menuItemRepository;
    private final MenuPermissionRepository menuPermissionRepository;
    private final MenuService menuService;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void run(String... args) {
        log.info("Initializing default menu items and permissions...");

        try {
            if (menuItemRepository.count() == 0) {
                log.info("No menu items found, creating default menu structure");
                createDefaultMenuItems();
                createDefaultMenuPermissions();
                log.info("Default menu items and permissions created successfully");
            } else {
                log.info("Menu items already exist, skipping initialization");
            }

            // Initialize menu permissions for all existing users (with error handling)
            initializeMenuPermissionsForAllUsersWithErrorHandling();

        } catch (Exception e) {
            log.error("Critical error during menu initialization: {}", e.getMessage(), e);
            // Don't re-throw to prevent application startup failure
        }
    }

    /**
     * AUTO-ASSIGN DEFAULT PERMISSIONS TO ALL EXISTING USERS WITH ERROR HANDLING
     */
    private void initializeMenuPermissionsForAllUsersWithErrorHandling() {
        try {
            log.info("Auto-initializing menu permissions for all existing users...");

            // Use a separate transaction to avoid rollback issues
            menuService.initializeMenuPermissionsForAllExistingUsers();
            log.info("Menu permission initialization completed for all existing users");

        } catch (Exception e) {
            log.error("Error during menu permission initialization: {}", e.getMessage());
            // Continue without throwing exception to allow application to start
        }
    }

    private void createDefaultMenuItems() {
        List<MenuItemEntity> menuItems = new ArrayList<>();
        Map<String, MenuItemEntity> menuMap = new HashMap<>();

        // Create main menu items
        MenuItemEntity dashboard = createMenuItem("DASHBOARD", "Dashboard", "/", "Home", 1, false);
        menuItems.add(dashboard);
        menuMap.put("DASHBOARD", dashboard);

        MenuItemEntity masterData = createMenuItem("MASTER_DATA", "Master data", null, "Master_Data", 2, true);
        menuItems.add(masterData);
        menuMap.put("MASTER_DATA", masterData);

        MenuItemEntity users = createMenuItem("USERS", "Users", null, "Users", 3, true);
        menuItems.add(users);
        menuMap.put("USERS", users);

        MenuItemEntity students = createMenuItem("STUDENTS", "Students", null, "Students", 4, true);
        menuItems.add(students);
        menuMap.put("STUDENTS", students);

        MenuItemEntity attendance = createMenuItem("ATTENDANCE", "Attendance", null, "Attendance", 5, true);
        menuItems.add(attendance);
        menuMap.put("ATTENDANCE", attendance);

        MenuItemEntity survey = createMenuItem("SURVEY", "Survey", null, "Survey", 6, true);
        menuItems.add(survey);
        menuMap.put("SURVEY", survey);

        MenuItemEntity scoreSubmitted = createMenuItem("SCORE_SUBMITTED", "Score submitted", null, "Scores_Submitted", 7, true);
        menuItems.add(scoreSubmitted);
        menuMap.put("SCORE_SUBMITTED", scoreSubmitted);

        MenuItemEntity studentScore = createMenuItem("STUDENT_SCORE", "Student score", "/student-score", "Student_Scores", 8, false);
        menuItems.add(studentScore);
        menuMap.put("STUDENT_SCORE", studentScore);

        MenuItemEntity schedule = createMenuItem("SCHEDULE", "Schedule", "/schedule", "Schedule", 9, false);
        menuItems.add(schedule);
        menuMap.put("SCHEDULE", schedule);

        MenuItemEntity manageSchedule = createMenuItem("MANAGE_SCHEDULE", "Manage schedule", "/manage-schedule/department", "Manage_Schedule", 10, false);
        menuItems.add(manageSchedule);
        menuMap.put("MANAGE_SCHEDULE", manageSchedule);

        MenuItemEntity request = createMenuItem("REQUEST", "Request", "/requests", "Request", 11, false);
        menuItems.add(request);
        menuMap.put("REQUEST", request);

        MenuItemEntity payment = createMenuItem("PAYMENT", "Payment", "/student-payment", "Payment", 12, false);
        menuItems.add(payment);
        menuMap.put("PAYMENT", payment);

        MenuItemEntity rolePermission = createMenuItem("ROLE_PERMISSION", "Role&User permission", "/permissions", "Role_Permission", 13, false);
        menuItems.add(rolePermission);
        menuMap.put("ROLE_PERMISSION", rolePermission);

        menuItemRepository.saveAll(menuItems);
        createChildMenuItems(menuMap);
    }

    private void createChildMenuItems(Map<String, MenuItemEntity> menuMap) {
        List<MenuItemEntity> childItems = new ArrayList<>();

        // Master Data children
        MenuItemEntity masterData = menuMap.get("MASTER_DATA");
        childItems.add(createChildMenuItem("MANAGE_CLASS", "Manage class", "/manage-class", masterData, 1));
        childItems.add(createChildMenuItem("MANAGE_SEMESTER", "Manage semester", "/manage-semester", masterData, 2));
        childItems.add(createChildMenuItem("MANAGE_MAJOR", "Manage major", "/manage-major", masterData, 3));
        childItems.add(createChildMenuItem("MANAGE_DEPARTMENT", "Manage department", "/manage-department", masterData, 4));
        childItems.add(createChildMenuItem("MANAGE_ROOM", "Manage room", "/manage-room", masterData, 5));
        childItems.add(createChildMenuItem("MANAGE_COURSE", "Manage Course", "/courses", masterData, 6));
        childItems.add(createChildMenuItem("MANAGE_SUBJECT", "Manage Subject", "/manage-subject", masterData, 7));

        // Users children
        MenuItemEntity users = menuMap.get("USERS");
        childItems.add(createChildMenuItem("ADMIN", "Admin", "/admin", users, 1));
        childItems.add(createChildMenuItem("STAFF_OFFICER", "Staff Officer", "/staff-officer", users, 2));
        childItems.add(createChildMenuItem("TEACHERS", "Teachers", "/teachers", users, 3));

        // Students children
        MenuItemEntity students = menuMap.get("STUDENTS");
        childItems.add(createChildMenuItem("ADD_MULTIPLE_USERS", "Add multiple users", "/add-multiple", students, 1));
        childItems.add(createChildMenuItem("ADD_SINGLE_USER", "Add single user", "/add-single", students, 2));
        childItems.add(createChildMenuItem("STUDENTS_LIST", "Students list", "/student-list", students, 3));

        // Attendance children
        MenuItemEntity attendance = menuMap.get("ATTENDANCE");
        childItems.add(createChildMenuItem("CLASS_SCHEDULE", "Class Schedule", "/attendance/schedule", attendance, 1));
        childItems.add(createChildMenuItem("HISTORY_RECORDS", "History Records", "/attendance/history-records", attendance, 2));
        childItems.add(createChildMenuItem("STUDENT_RECORDS", "Student Records", "/attendance/student-records", attendance, 3));

        // Survey children
        MenuItemEntity survey = menuMap.get("SURVEY");
        childItems.add(createChildMenuItem("RESULT_LIST", "Result List", "/survey-result", survey, 1));
        childItems.add(createChildMenuItem("MANAGE_QA", "Manage Q&As", "/manage-question", survey, 2));
        childItems.add(createChildMenuItem("SURVEY_STUDENT_RECORDS", "Student Records", "/survey/student-records", survey, 3));
        childItems.add(createChildMenuItem("SURVEY_STUDENT", "Student", "/survey/student", survey, 4));

        // Score Submitted children
        MenuItemEntity scoreSubmitted = menuMap.get("SCORE_SUBMITTED");
        childItems.add(createChildMenuItem("SUBMITTED_LIST", "Submitted List", "/submitted-list", scoreSubmitted, 1));
        childItems.add(createChildMenuItem("SCORE_SETTING", "Score Setting", "/score-setting", scoreSubmitted, 2));

        menuItemRepository.saveAll(childItems);
    }

    private MenuItemEntity createMenuItem(String code, String title, String route, String icon, int displayOrder, boolean isParent) {
        MenuItemEntity menuItem = new MenuItemEntity();
        menuItem.setCode(code);
        menuItem.setTitle(title);
        menuItem.setRoute(route);
        menuItem.setIcon(icon);
        menuItem.setDisplayOrder(displayOrder);
        menuItem.setStatus(Status.ACTIVE);
        menuItem.setIsParent(isParent);
        return menuItem;
    }

    private MenuItemEntity createChildMenuItem(String code, String title, String route, MenuItemEntity parent, int displayOrder) {
        MenuItemEntity menuItem = new MenuItemEntity();
        menuItem.setCode(code);
        menuItem.setTitle(title);
        menuItem.setRoute(route);
        menuItem.setDisplayOrder(displayOrder);
        menuItem.setStatus(Status.ACTIVE);
        menuItem.setIsParent(false);
        menuItem.setParent(parent);
        return menuItem;
    }

    private void createDefaultMenuPermissions() {
        List<MenuItemEntity> allMenuItems = menuItemRepository.findAll();
        List<MenuPermissionEntity> permissions = new ArrayList<>();

        for (MenuItemEntity menuItem : allMenuItems) {
            // Create role-based permissions for fallback with FIXED permissions
            switch (menuItem.getCode()) {
                case "DASHBOARD":
                    permissions.addAll(createPermissionsForRoles(menuItem,
                            List.of(RoleEnum.DEVELOPER, RoleEnum.ADMIN, RoleEnum.STAFF, RoleEnum.TEACHER, RoleEnum.STUDENT)));
                    break;
                case "MASTER_DATA":
                case "MANAGE_CLASS":
                case "MANAGE_SEMESTER":
                case "MANAGE_MAJOR":
                case "MANAGE_DEPARTMENT":
                case "MANAGE_ROOM":
                case "MANAGE_COURSE":
                case "MANAGE_SUBJECT":
                    permissions.addAll(createPermissionsForRoles(menuItem,
                            List.of(RoleEnum.DEVELOPER, RoleEnum.ADMIN)));
                    break;
                case "USERS":
                case "ADMIN":
                case "STAFF_OFFICER":
                case "TEACHERS":
                    permissions.addAll(createPermissionsForRoles(menuItem,
                            List.of(RoleEnum.DEVELOPER, RoleEnum.ADMIN)));
                    break;
                case "STUDENTS":
                case "ADD_MULTIPLE_USERS":
                case "ADD_SINGLE_USER":
                case "STUDENTS_LIST":
                    permissions.addAll(createPermissionsForRoles(menuItem,
                            List.of(RoleEnum.DEVELOPER, RoleEnum.ADMIN, RoleEnum.STAFF, RoleEnum.TEACHER)));
                    break;
                // ✅ FIXED: Admin and Developer now get Attendance access
                case "ATTENDANCE":
                case "CLASS_SCHEDULE":
                case "HISTORY_RECORDS":
                case "STUDENT_RECORDS":
                    permissions.addAll(createPermissionsForRoles(menuItem,
                            List.of(RoleEnum.DEVELOPER, RoleEnum.ADMIN, RoleEnum.STAFF, RoleEnum.TEACHER)));
                    break;
                // ✅ FIXED: Admin and Developer now get Survey access
                case "SURVEY":
                case "RESULT_LIST":
                case "MANAGE_QA":
                case "SURVEY_STUDENT_RECORDS":
                    permissions.addAll(createPermissionsForRoles(menuItem,
                            List.of(RoleEnum.DEVELOPER, RoleEnum.ADMIN, RoleEnum.STAFF, RoleEnum.TEACHER)));
                    break;
                case "SURVEY_STUDENT":
                    permissions.addAll(createPermissionsForRoles(menuItem,
                            List.of(RoleEnum.DEVELOPER, RoleEnum.ADMIN, RoleEnum.STAFF, RoleEnum.TEACHER, RoleEnum.STUDENT)));
                    break;
                // ✅ FIXED: Admin and Developer now get Score access
                case "SCORE_SUBMITTED":
                case "SUBMITTED_LIST":
                case "SCORE_SETTING":
                case "STUDENT_SCORE":
                    permissions.addAll(createPermissionsForRoles(menuItem,
                            List.of(RoleEnum.DEVELOPER, RoleEnum.ADMIN, RoleEnum.STAFF, RoleEnum.TEACHER)));
                    break;
                case "SCHEDULE":
                    permissions.addAll(createPermissionsForRoles(menuItem,
                            List.of(RoleEnum.DEVELOPER, RoleEnum.ADMIN, RoleEnum.STAFF, RoleEnum.TEACHER, RoleEnum.STUDENT)));
                    break;
                case "MANAGE_SCHEDULE":
                    permissions.addAll(createPermissionsForRoles(menuItem,
                            List.of(RoleEnum.DEVELOPER, RoleEnum.ADMIN)));
                    break;
                case "REQUEST":
                    permissions.addAll(createPermissionsForRoles(menuItem,
                            List.of(RoleEnum.DEVELOPER, RoleEnum.ADMIN)));
                    break;
                case "PAYMENT":
                    permissions.addAll(createPermissionsForRoles(menuItem,
                            List.of(RoleEnum.DEVELOPER, RoleEnum.ADMIN, RoleEnum.STAFF, RoleEnum.TEACHER, RoleEnum.STUDENT)));
                    break;
                case "ROLE_PERMISSION":
                    permissions.addAll(createPermissionsForRoles(menuItem,
                            List.of(RoleEnum.DEVELOPER)));
                    break;
                default:
                    permissions.addAll(createPermissionsForRoles(menuItem,
                            List.of(RoleEnum.DEVELOPER, RoleEnum.ADMIN)));
                    break;
            }
        }

        menuPermissionRepository.saveAll(permissions);
        log.info("Created {} default role-based menu permissions", permissions.size());
    }

    private List<MenuPermissionEntity> createPermissionsForRoles(MenuItemEntity menuItem, List<RoleEnum> roles) {
        List<MenuPermissionEntity> permissions = new ArrayList<>();

        for (RoleEnum role : roles) {
            MenuPermissionEntity permission = new MenuPermissionEntity();
            permission.setMenuItem(menuItem);
            permission.setRole(role); // Role-based permission
            permission.setUser(null);  // No user = role-based
            permission.setCanView(true);
            permission.setDisplayOrder(menuItem.getDisplayOrder());
            permission.setStatus(Status.ACTIVE);
            permissions.add(permission);
        }

        return permissions;
    }
}