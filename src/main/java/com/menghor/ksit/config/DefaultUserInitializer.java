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
            Collections.singletonList(RoleEnum.DEVELOPER),
            "System", 
            "Developer",
            "123456789",
            null, 
            null, 
            null,
            "Lead Developer", 
            "IT Department", 
            "DEV001"
        );

        // Create admin user
        createUser(
            "admin@ksit.com",
            "admin123",
            Collections.singletonList(RoleEnum.ADMIN),
            "School", 
            "Administrator",
            "123456789",
            null, 
            null, 
            null,
            "Principal", 
            "Administration", 
            "ADM001"
        );

        // Create a staff user
        createUser(
            "staff@ksit.com",
            "staff123",
            Collections.singletonList(RoleEnum.STAFF),
            "John", 
            "Smith",
            "123456789",
            null, 
            null, 
            null,
            "Professor", 
            "Computer Science", 
            "STF001"
        );

        // Create a student user
        createUser(
            "student@ksit.com",
            "student123",
            Collections.singletonList(RoleEnum.STUDENT),
            "Jane", 
            "Doe",
            "123456789",
            "STU001", 
            "Year 1", 
            2024,
            null, 
            null, 
            null
        );
        
        // Create a multi-role user (staff + admin)
        createUser(
            "headteacher@ksit.com",
            "headteacher123",
            Arrays.asList(RoleEnum.STAFF, RoleEnum.ADMIN),
            "Head", 
            "Teacher",
            "123456789",
            null, 
            null, 
            null,
            "Head Teacher", 
            "Academic Affairs", 
            "HT001"
        );

        log.info("Default users created successfully");
    }
    
    private void createUser(
            String email, 
            String password, 
            List<RoleEnum> roleEnums,
            String firstName,
            String lastName,
            String contactNumber,
            String studentId,
            String grade,
            Integer yearOfAdmission,
            String position,
            String department,
            String employeeId
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
        
        // Set common fields
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setContactNumber(contactNumber);
        
        // Set student-specific fields if applicable
        user.setStudentId(studentId);
        user.setGrade(grade);
        user.setYearOfAdmission(yearOfAdmission);
        
        // Set staff-specific fields if applicable
        user.setPosition(position);
        user.setDepartment(department);
        user.setEmployeeId(employeeId);

        userRepository.save(user);
        log.info("Created default user: {}", email);
    }
}