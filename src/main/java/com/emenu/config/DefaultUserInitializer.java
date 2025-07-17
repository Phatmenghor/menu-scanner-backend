package com.emenu.config;

import com.emenu.enumations.RoleEnum;
import com.emenu.enumations.Status;
import com.emenu.feature.auth.models.Role;
import com.emenu.feature.auth.models.UserEntity;
import com.emenu.feature.auth.repository.RoleRepository;
import com.emenu.feature.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(2) // Run after DefaultRoleInitializer
public class DefaultUserInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Checking for default users...");
        if (userRepository.count() > 0) {
            log.info("Users already exist, skipping default user creation");
            return;
        }
        log.info("No users found, creating default users");
        createDeveloperUser();
        createAdminUser();
        createStaffUser();
        createTeacherUser();
        createStudentUser();
        createMultiRoleUser();
        log.info("Default users created successfully");
    }

    private void createDeveloperUser() {
        UserEntity developer = new UserEntity();
        developer.setUsername("phatmenghor19@gmail.com");
        developer.setEmail("phatmenghor19@gmail.com");
        developer.setPassword(passwordEncoder.encode("88889999"));
        developer.setStatus(Status.ACTIVE);

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
        admin.setPassword(passwordEncoder.encode("88889999"));
        admin.setStatus(Status.ACTIVE);

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
        staff.setPassword(passwordEncoder.encode("88889999"));
        staff.setStatus(Status.ACTIVE);

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
        teacher.setPassword(passwordEncoder.encode("88889999"));
        teacher.setStatus(Status.ACTIVE);

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
        student.setPassword(passwordEncoder.encode("88889999"));
        student.setStatus(Status.ACTIVE);

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
        headTeacher.setPassword(passwordEncoder.encode("88889999"));
        headTeacher.setStatus(Status.ACTIVE);

        Role staffRole = roleRepository.findByName(RoleEnum.STAFF)
                .orElseThrow(() -> new RuntimeException("Role not found: " + RoleEnum.STAFF));
        Role adminRole = roleRepository.findByName(RoleEnum.ADMIN)
                .orElseThrow(() -> new RuntimeException("Role not found: " + RoleEnum.TEACHER));
        headTeacher.setRoles(Arrays.asList(staffRole, adminRole));

        userRepository.save(headTeacher);
        log.info("Created multi-role user: headteacher@ksit.com");
    }
}