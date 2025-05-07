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
import java.util.List;

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
        createUser(
            "developer@ksit.com",
            "developer123",
            Collections.singletonList(RoleEnum.DEVELOPER)
        );

        // Create admin user
        createUser(
            "admin@ksit.com",
            "admin123",
            Collections.singletonList(RoleEnum.ADMIN)

        );

        // Create a staff user
        createUser(
            "staff@ksit.com",
            "staff123",
            Collections.singletonList(RoleEnum.STAFF)

        );

        // Create a student user
        createUser(
            "student@ksit.com",
            "student123",
            Collections.singletonList(RoleEnum.STUDENT)
        );
        
        // Create a multi-role user (staff + admin)
        createUser(
            "headteacher@ksit.com",
            "headteacher123",
            Arrays.asList(RoleEnum.STAFF, RoleEnum.ADMIN)
        );

        log.info("Default users created successfully");
    }
    
    private void createUser(
            String email, 
            String password, 
            List<RoleEnum> roleEnums
    ) {
        List<Role> roles = roleEnums.stream()
                .map(roleEnum -> roleRepository.findByName(roleEnum)
                        .orElseThrow(() -> new RuntimeException("Role not found: " + roleEnum)))
                .toList();

        UserEntity user = new UserEntity();
        user.setUsername(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles(roles);
        user.setStatus(Status.ACTIVE);

        userRepository.save(user);
        log.info("Created default user: {}", email);
    }
}