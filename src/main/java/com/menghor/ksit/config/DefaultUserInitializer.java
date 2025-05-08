package com.menghor.ksit.config;

import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.feature.auth.models.Role;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.auth.repository.RoleRepository;
import com.menghor.ksit.feature.auth.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;

@Component
@RequiredArgsConstructor
@Slf4j
public class DefaultUserInitializer {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @EventListener(ContextRefreshedEvent.class)
    @Transactional
    public void initDefaultUsers() {
        log.info("Checking for default users...");

        // Skip if users already exist
        if (userRepository.count() > 0) {
            log.info("Users already exist, skipping default user creation");
            return;
        }

        log.info("No users found, creating default users");

        // Create developer superuser
        createDeveloperUser();

        // Create admin user
        createAdminUser();

        // Create a staff user
        createStaffUser();

        // Create a teacher user
        createTeacherUser();

        // Create a student user
        createStudentUser();

        // Create a multi-role user (staff + admin)
        createMultiRoleUser();

        log.info("Default users created successfully");
    }

    private void createDeveloperUser() {
        UserEntity developer = new UserEntity();
        developer.setUsername("developer@ksit.com");
        developer.setEmail("developer@ksit.com");
        developer.setPassword(passwordEncoder.encode("developer123"));
        developer.setStatus(Status.ACTIVE);

        // Set role
        Role devRole = roleRepository.findByName(RoleEnum.DEVELOPER)
                .orElseThrow(() -> new RuntimeException("Role not found: " + RoleEnum.DEVELOPER));
        developer.setRoles(Collections.singletonList(devRole));

        userRepository.save(developer);
        log.info("Created developer user: developer@ksit.com");
    }

    private void createAdminUser() {
        UserEntity admin = new UserEntity();
        admin.setUsername("admin@ksit.com");
        admin.setEmail("admin@ksit.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setStatus(Status.ACTIVE);

        // Set role
        Role adminRole = roleRepository.findByName(RoleEnum.ADMIN)
                .orElseThrow(() -> new RuntimeException("Role not found: " + RoleEnum.ADMIN));
        admin.setRoles(Collections.singletonList(adminRole));

        userRepository.save(admin);
        log.info("Created admin user: admin@ksit.com");
    }

    private void createStaffUser() {
        UserEntity staff = new UserEntity();
        staff.setUsername("staff@ksit.com");
        staff.setEmail("staff@ksit.com");
        staff.setPassword(passwordEncoder.encode("staff123"));
        staff.setStatus(Status.ACTIVE);

        // Set role
        Role staffRole = roleRepository.findByName(RoleEnum.STAFF)
                .orElseThrow(() -> new RuntimeException("Role not found: " + RoleEnum.STAFF));
        staff.setRoles(Collections.singletonList(staffRole));

        userRepository.save(staff);
        log.info("Created staff user: staff@ksit.com");
    }

    private void createTeacherUser() {
        UserEntity teacher = new UserEntity();
        teacher.setUsername("teacher@ksit.com");
        teacher.setEmail("teacher@ksit.com");
        teacher.setPassword(passwordEncoder.encode("teacher123"));
        teacher.setStatus(Status.ACTIVE);

        // Set role
        Role teacherRole = roleRepository.findByName(RoleEnum.TEACHER)
                .orElseThrow(() -> new RuntimeException("Role not found: " + RoleEnum.TEACHER));
        teacher.setRoles(Collections.singletonList(teacherRole));

        userRepository.save(teacher);
        log.info("Created teacher user: teacher@ksit.com");
    }

    private void createStudentUser() {
        UserEntity student = new UserEntity();
        student.setUsername("student@ksit.com");
        student.setEmail("student@ksit.com");
        student.setPassword(passwordEncoder.encode("student123"));
        student.setStatus(Status.ACTIVE);

        // Set role
        Role studentRole = roleRepository.findByName(RoleEnum.STUDENT)
                .orElseThrow(() -> new RuntimeException("Role not found: " + RoleEnum.STUDENT));
        student.setRoles(Collections.singletonList(studentRole));

        userRepository.save(student);
        log.info("Created student user: student@ksit.com");
    }

    private void createMultiRoleUser() {
        UserEntity headTeacher = new UserEntity();
        headTeacher.setUsername("headteacher@ksit.com");
        headTeacher.setEmail("headteacher@ksit.com");
        headTeacher.setPassword(passwordEncoder.encode("headteacher123"));
        headTeacher.setStatus(Status.ACTIVE);

        // Set multiple roles
        Role staffRole = roleRepository.findByName(RoleEnum.STAFF)
                .orElseThrow(() -> new RuntimeException("Role not found: " + RoleEnum.STAFF));
        Role adminRole = roleRepository.findByName(RoleEnum.ADMIN)
                .orElseThrow(() -> new RuntimeException("Role not found: " + RoleEnum.ADMIN));
        headTeacher.setRoles(Arrays.asList(staffRole, adminRole));

        userRepository.save(headTeacher);
        log.info("Created multi-role user: headteacher@ksit.com");
    }
}