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
import com.menghor.ksit.feature.auth.dto.resposne.UserAllResponseDto;
import com.menghor.ksit.feature.auth.dto.resposne.UserDetailsDto;
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
    public UserAllResponseDto getAllUsers(UserFilterDto filterDto) {
        log.info("Fetching all users with filter: {}", filterDto);
        return getUsersWithSpecification(filterDto, UserSpecification::createAllRolesSpecification);
    }

    @Override
    public UserAllResponseDto getAllUsersIncludingShopAdmin(UserFilterDto filterDto) {
        log.info("Fetching all users including shop admin with filter: {}", filterDto);
        return getAllUsers(filterDto);
    }

    private UserAllResponseDto getUsersWithSpecification(
            UserFilterDto filterDto,
            SpecificationCreator specificationCreator
    ) {
        // Set default values if null
        if (filterDto.getPageNo() == null) filterDto.setPageNo(1);
        if (filterDto.getPageSize() == null) filterDto.setPageSize(10);

        // Validate pagination parameters
        PaginationUtils.validatePagination(filterDto.getPageNo(), filterDto.getPageSize());

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

        userPage.getContent().forEach(user -> {
            if (user.getStatus() == null) {
                user.setStatus(Status.ACTIVE); // Default to ACTIVE
                userRepository.save(user);
            }
        });

        // Convert to DTOs
        List<UserDetailsDto> userDtos = userPage.getContent().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());

        return userMapper.toPageDto(userDtos, userPage);
    }

    @FunctionalInterface
    private interface SpecificationCreator {
        Specification<UserEntity> createSpec(String username, Status status, RoleEnum role);
    }

    @Override
    public UserDetailsDto getUserById(Long id) {
        log.info("Fetching user by ID: {}", id);
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User with ID {} not found", id);
                    return new NotFoundException(String.format(ErrorMessages.USER_NOT_FOUND, id));
                });

        // Ensure status is set
        if (user.getStatus() == null) {
            user.setStatus(Status.ACTIVE);
            userRepository.save(user);
        }

        return userMapper.toDto(user);
    }

    @Override
    public UserDetailsDto getUserByToken() {
        UserEntity currentUser = securityUtils.getCurrentUser();
        log.info("Fetching current user details for user ID: {}", currentUser.getId());

        if (currentUser.getStatus() == null) {
            currentUser.setStatus(Status.ACTIVE);
            userRepository.save(currentUser);
        }

        return userMapper.toDto(currentUser);
    }

    @Override
    @Transactional
    public UserDetailsDto updateUser(Long id, UserUpdateDto updateDto) {
        log.info("Updating user with ID: {}", id);

        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User with ID {} not found", id);
                    return new NotFoundException(String.format(ErrorMessages.USER_NOT_FOUND, id));
                });

        userMapper.updateUserFromDto(updateDto, user);

        if (updateDto.getRoles() != null && !updateDto.getRoles().isEmpty()) {
            List<Role> roles = new ArrayList<>();
            for (RoleEnum roleEnum : updateDto.getRoles()) {
                Role role = roleRepository.findByName(roleEnum)
                        .orElseThrow(() -> {
                            log.error("Role {} not found", roleEnum);
                            return new NotFoundException("Role not found: " + roleEnum);
                        });
                roles.add(role);
            }
            user.setRoles(roles);
        } else if (updateDto.getRole() != null) {
            Role role = roleRepository.findByName(updateDto.getRole())
                    .orElseThrow(() -> {
                        log.error("Role {} not found", updateDto.getRole());
                        return new NotFoundException("Role not found: " + updateDto.getRole());
                    });
            user.getRoles().clear();
            user.getRoles().add(role);
        }

        if (user.getStatus() == null) {
            user.setStatus(updateDto.getStatus() != null ? updateDto.getStatus() : Status.ACTIVE);
        }

        UserEntity updatedUser = userRepository.save(user);
        log.info("User with ID {} updated successfully", id);

        return userMapper.toDto(updatedUser);
    }

    @Transactional
    @Override
    public UserDetailsDto deleteUserId(Long id) {
        log.info("Deleting user with ID: {}", id);
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User with ID {} not found", id);
                    return new NotFoundException(String.format(ErrorMessages.USER_NOT_FOUND, id));
                });

        user.getRoles().clear();
        userRepository.deleteById(id);
        log.info("User with ID {} deleted successfully", id);

        return userMapper.toDto(user);
    }

    @Override
    public UserDetailsDto changePassword(ChangePasswordRequestDto requestDto) {
        log.info("Changing password for current user");

        UserEntity user = securityUtils.getCurrentUser();

        if (!passwordEncoder.matches(requestDto.getCurrentPassword(), user.getPassword())) {
            log.warn("Current password is incorrect for user ID: {}", user.getId());
            throw new BadRequestException(ErrorMessages.CURRENT_PASSWORD_INCORRECT);
        }

        if (!requestDto.getNewPassword().equals(requestDto.getConfirmNewPassword())) {
            log.warn("New passwords do not match for user ID: {}", user.getId());
            throw new BadRequestException(ErrorMessages.PASSWORDS_DO_NOT_MATCH);
        }

        user.setPassword(passwordEncoder.encode(requestDto.getNewPassword()));

        if (user.getStatus() == null) {
            user.setStatus(Status.ACTIVE);
        }

        UserEntity updatedUser = userRepository.save(user);
        log.info("Password changed successfully for user ID: {}", user.getId());

        return userMapper.toDto(updatedUser);
    }

    @Override
    public UserDetailsDto changePasswordByAdmin(ChangePasswordByAdminRequestDto requestDto) {
        log.info("Changing password for user ID: {}", requestDto.getId());

        UserEntity user = userRepository.findById(requestDto.getId())
                .orElseThrow(() -> {
                    log.error("User with ID {} not found", requestDto.getId());
                    return new NotFoundException(String.format(ErrorMessages.USER_NOT_FOUND, requestDto.getId()));
                });

        if (!requestDto.getNewPassword().equals(requestDto.getConfirmNewPassword())) {
            log.warn("New passwords do not match for user ID: {}", requestDto.getId());
            throw new BadRequestException(ErrorMessages.PASSWORDS_DO_NOT_MATCH);
        }

        user.setPassword(passwordEncoder.encode(requestDto.getNewPassword()));

        if (user.getStatus() == null) {
            user.setStatus(Status.ACTIVE);
        }

        UserEntity updatedUser = userRepository.save(user);
        log.info("Password changed successfully for user ID: {}", requestDto.getId());

        return userMapper.toDto(updatedUser);
    }
}
