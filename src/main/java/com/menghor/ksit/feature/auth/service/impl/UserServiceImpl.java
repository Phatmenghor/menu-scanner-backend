package com.menghor.ksit.feature.auth.service.impl;

import com.menghor.ksit.constants.ErrorMessages;
import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.exceptoins.error.BadRequestException;
import com.menghor.ksit.exceptoins.error.NotFoundException;
import com.menghor.ksit.feature.auth.dto.request.ChangePasswordByAdminRequestDto;
import com.menghor.ksit.feature.auth.dto.request.ChangePasswordRequestDto;
import com.menghor.ksit.feature.auth.dto.request.UserFilterDto;
import com.menghor.ksit.feature.auth.dto.request.UserUpdateDto;
import com.menghor.ksit.feature.auth.dto.resposne.UserDto;
import com.menghor.ksit.feature.auth.dto.resposne.UserResponseDto;
import com.menghor.ksit.feature.auth.mapper.UserMapper;
import com.menghor.ksit.feature.auth.models.Role;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.auth.repository.RoleRepository;
import com.menghor.ksit.feature.auth.repository.UserRepository;
import com.menghor.ksit.feature.auth.repository.UserSpecification;
import com.menghor.ksit.feature.auth.service.UserService;
import com.menghor.ksit.utils.database.SecurityUtils;
import com.menghor.ksit.utils.pagiantion.PaginationUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;

    @Override
    public UserResponseDto getAllUsers(UserFilterDto filterDto) {
        // Default method: includes all users
        return getUsersWithSpecification(filterDto, UserSpecification::createAllRolesSpecification);
    }

    @Override
    public UserResponseDto getAllUsersIncludingShopAdmin(UserFilterDto filterDto) {
        // This method now does the same as getAllUsers for backward compatibility
        return getAllUsers(filterDto);
    }

    private UserResponseDto getUsersWithSpecification(
            UserFilterDto filterDto,
            SpecificationCreator specificationCreator
    ) {
        // Set default values if null
        if (filterDto.getPageNo() == null) filterDto.setPageNo(1);
        if (filterDto.getPageSize() == null) filterDto.setPageSize(10);

        // Validate pagination parameters
        PaginationUtils.validatePagination(filterDto.getPageNo(), filterDto.getPageSize());

        // Adjust page number for 0-based indexing
        int pageNo = filterDto.getPageNo() - 1;
        int pageSize = filterDto.getPageSize();
        Pageable pageable = PageRequest.of(pageNo, pageSize);

        // Create specification with filter criteria
        Specification<UserEntity> spec = specificationCreator.createSpec(
                filterDto.getSearch(),
                filterDto.getStatus(),
                filterDto.getRole()
        );

        log.info("Fetching users with filters: {}", filterDto);

        // Find users with specification
        Page<UserEntity> userPage = userRepository.findAll(spec, pageable);

        log.info("Found {} users", userPage.getContent().size());

        // Ensure all users have a status value
        userPage.getContent().forEach(user -> {
            if (user.getStatus() == null) {
                user.setStatus(Status.ACTIVE); // Default to ACTIVE
                userRepository.save(user);
            }
        });

        // Convert to DTOs
        List<UserDto> userDtos = userPage.getContent().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());

        // Create the UserResponseDto
        return userMapper.toPageDto(userDtos, userPage);
    }

    // Functional interface for specification creation
    @FunctionalInterface
    private interface SpecificationCreator {
        Specification<UserEntity> createSpec(String username, Status status, RoleEnum role);
    }

    @Override
    public UserDto getUserById(Long id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format(ErrorMessages.USER_NOT_FOUND, id)));

        // Ensure status is set
        if (user.getStatus() == null) {
            user.setStatus(Status.ACTIVE);
            userRepository.save(user);
        }

        return userMapper.toDto(user);
    }

    @Override
    public UserDto getUserByToken() {
        UserEntity currentUser = securityUtils.getCurrentUser();

        // Ensure status is set
        if (currentUser.getStatus() == null) {
            currentUser.setStatus(Status.ACTIVE);
            userRepository.save(currentUser);
        }

        return userMapper.toDto(currentUser);
    }

    @Override
    @Transactional
    public UserDto updateUser(Long id, UserUpdateDto updateDto) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format(ErrorMessages.USER_NOT_FOUND, id)));

        // Update basic fields from DTO if provided
        userMapper.updateUserFromDto(updateDto, user);

        // Handle roles update - there are two possibilities:
        // 1. Multiple roles via dto.getRoles()
        // 2. Single role via dto.getRole() (for backward compatibility)

        if (updateDto.getRoles() != null && !updateDto.getRoles().isEmpty()) {
            // Handle multiple roles
            List<Role> roles = new ArrayList<>();
            for (RoleEnum roleEnum : updateDto.getRoles()) {
                Role role = roleRepository.findByName(roleEnum)
                        .orElseThrow(() -> new NotFoundException("Role not found: " + roleEnum));
                roles.add(role);
            }
            user.setRoles(roles);
        } else if (updateDto.getRole() != null) {
            // Handle single role (backward compatibility)
            Role role = roleRepository.findByName(updateDto.getRole())
                    .orElseThrow(() -> new NotFoundException("Role not found: " + updateDto.getRole()));
            user.getRoles().clear();
            user.getRoles().add(role);
        }

        // Ensure status is set
        if (user.getStatus() == null) {
            user.setStatus(updateDto.getStatus() != null ? updateDto.getStatus() : Status.ACTIVE);
        }

        UserEntity updatedUser = userRepository.save(user);
        return userMapper.toDto(updatedUser);
    }

    @Transactional
    @Override
    public UserDto deleteUserId(Long id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format(ErrorMessages.USER_NOT_FOUND, id)));
        user.getRoles().clear();
        userRepository.deleteById(id);
        return userMapper.toDto(user);
    }

    @Override
    public UserDto changePassword(ChangePasswordRequestDto requestDto) {
        UserEntity user = securityUtils.getCurrentUser();

        if (!passwordEncoder.matches(requestDto.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException(ErrorMessages.CURRENT_PASSWORD_INCORRECT);
        }

        if (!requestDto.getNewPassword().equals(requestDto.getConfirmNewPassword())) {
            throw new BadRequestException(ErrorMessages.PASSWORDS_DO_NOT_MATCH);
        }

        user.setPassword(passwordEncoder.encode(requestDto.getNewPassword()));

        // Ensure status is set
        if (user.getStatus() == null) {
            user.setStatus(Status.ACTIVE);
        }

        UserEntity userEntity = userRepository.save(user);
        return userMapper.toDto(userEntity);
    }

    @Override
    public UserDto changePasswordByAdmin(ChangePasswordByAdminRequestDto requestDto) {
        UserEntity user = userRepository.findById(requestDto.getId())
                .orElseThrow(() -> {
                    log.error("User with id {} not found", requestDto.getId());
                    return new NotFoundException(String.format(ErrorMessages.USER_NOT_FOUND, requestDto.getId()));
                });

        // Optionally, verify that new password and confirm password match
        if (!requestDto.getNewPassword().equals(requestDto.getConfirmNewPassword())) {
            throw new BadRequestException(ErrorMessages.PASSWORDS_DO_NOT_MATCH);
        }

        user.setPassword(passwordEncoder.encode(requestDto.getNewPassword()));

        // Ensure status is set
        if (user.getStatus() == null) {
            user.setStatus(Status.ACTIVE);
        }

        UserEntity userEntity = userRepository.save(user);
        return userMapper.toDto(userEntity);
    }
}