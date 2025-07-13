package com.menghor.ksit.feature.auth.service.impl;

import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.exceptoins.error.BadRequestException;
import com.menghor.ksit.exceptoins.error.NotFoundException;
import com.menghor.ksit.feature.auth.dto.resposne.UserRoleResponseDto;
import com.menghor.ksit.feature.auth.dto.update.UserRoleUpdateRequestDto;
import com.menghor.ksit.feature.auth.models.Role;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.auth.repository.RoleRepository;
import com.menghor.ksit.feature.auth.repository.UserRepository;
import com.menghor.ksit.feature.auth.service.StaffTeacherRoleService;
import com.menghor.ksit.feature.menu.service.MenuService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StaffTeacherRoleServiceImpl implements StaffTeacherRoleService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final MenuService menuService;

    @Override
    public List<UserRoleResponseDto> getUserStaffTeacherRoles(Long userId) {
        log.info("Getting STAFF/TEACHER roles for user ID: {}", userId);

        UserEntity user = getUserById(userId);
        
        // Get current user roles
        List<RoleEnum> userRoles = user.getRoles().stream()
                .map(Role::getName)
                .toList();

        // Always return 2 objects for easy frontend loop
        List<UserRoleResponseDto> result = Arrays.asList(
            UserRoleResponseDto.builder()
                .role(RoleEnum.STAFF)
                .name("Staff")
                .hasRole(userRoles.contains(RoleEnum.STAFF))
                .build(),
            UserRoleResponseDto.builder()
                .role(RoleEnum.TEACHER)
                .name("Teacher")
                .hasRole(userRoles.contains(RoleEnum.TEACHER))
                .build()
        );

        log.info("User ID: {} role status - STAFF: {}, TEACHER: {}", 
                userId, result.get(0).getHasRole(), result.get(1).getHasRole());
        return result;
    }

    @Override
    @Transactional
    public List<UserRoleResponseDto> updateUserStaffTeacherRoles(Long userId, UserRoleUpdateRequestDto updateDto) {
        log.info("Updating STAFF/TEACHER roles for user ID: {} to: {}", userId, updateDto.getRoles());

        UserEntity user = getUserById(userId);

        // Validate that only STAFF and TEACHER roles are being assigned
        List<RoleEnum> invalidRoles = updateDto.getRoles().stream()
                .filter(role -> role != RoleEnum.STAFF && role != RoleEnum.TEACHER)
                .collect(Collectors.toList());

        if (!invalidRoles.isEmpty()) {
            throw new BadRequestException("Only STAFF and TEACHER roles allowed. Invalid: " + invalidRoles);
        }

        // Get the new STAFF/TEACHER role entities
        List<Role> newStaffTeacherRoles = new ArrayList<>();
        for (RoleEnum roleEnum : updateDto.getRoles()) {
            Role role = roleRepository.findByName(roleEnum)
                    .orElseThrow(() -> new BadRequestException("Role not found: " + roleEnum));
            newStaffTeacherRoles.add(role);
        }

        // Keep existing non-STAFF/TEACHER roles (ADMIN, DEVELOPER, STUDENT)
        List<Role> existingOtherRoles = user.getRoles().stream()
                .filter(role -> role.getName() != RoleEnum.STAFF && role.getName() != RoleEnum.TEACHER)
                .collect(Collectors.toList());

        // Final roles = Other roles + New STAFF/TEACHER roles
        List<Role> finalRoles = new ArrayList<>(existingOtherRoles);
        finalRoles.addAll(newStaffTeacherRoles);
        
        user.setRoles(finalRoles);
        userRepository.save(user);

        // Refresh menu permissions
        try {
            menuService.refreshUserMenuPermissionsAfterRoleChange(userId);
            log.info("Menu permissions refreshed for user: {}", userId);
        } catch (Exception e) {
            log.error("Failed to refresh menu permissions for user {}: {}", userId, e.getMessage());
        }

        // Return always 2 objects with updated hasRole flags
        List<UserRoleResponseDto> result = Arrays.asList(
            UserRoleResponseDto.builder()
                .role(RoleEnum.STAFF)
                .name("Staff")
                .hasRole(updateDto.getRoles().contains(RoleEnum.STAFF))
                .build(),
            UserRoleResponseDto.builder()
                .role(RoleEnum.TEACHER)
                .name("Teacher")
                .hasRole(updateDto.getRoles().contains(RoleEnum.TEACHER))
                .build()
        );

        log.info("Successfully updated roles for user ID: {} - STAFF: {}, TEACHER: {}", 
                userId, result.get(0).getHasRole(), result.get(1).getHasRole());
        return result;
    }

    private UserEntity getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + userId));
    }
}