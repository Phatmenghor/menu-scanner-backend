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
import com.menghor.ksit.feature.setting.mapper.SubscriptionMapper;
import com.menghor.ksit.feature.setting.repository.SubscriptionRepository;
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
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionMapper subscriptionMapper;

    @Override
    public UserResponseDto getAllUsers(UserFilterDto filterDto) {
        // Default method: excludes SHOP_ADMIN
        return getUsersWithSpecification(filterDto, UserSpecification::createSpecification);
    }

    @Override
    public UserResponseDto getAllUsersIncludingShopAdmin(UserFilterDto filterDto) {
        // Method to get ALL users including SHOP_ADMIN
        return getUsersWithSpecification(filterDto, UserSpecification::createAllRolesSpecification);
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

        log.info("Found {} users before subscription filtering", userPage.getContent().size());

        // Ensure all users have a status value
        userPage.getContent().forEach(user -> {
            if (user.getStatus() == null) {
                user.setStatus(Status.ACTIVE); // Default to ACTIVE
                userRepository.save(user);
            }
        });

        // Use the bulk enrichment method in UserMapper
        List<UserDto> userDtos = userMapper.enrichUsersWithSubscriptions(userPage.getContent());

        // If subscription filter is applied, filter the list manually
        if (filterDto.getHasActiveSubscription() != null) {
            boolean wantActive = filterDto.getHasActiveSubscription();
            List<UserDto> filteredUsers = userDtos.stream()
                    .filter(user -> user.getHasActiveSubscription() == wantActive)
                    .collect(Collectors.toList());

            log.info("After subscription filtering: {} users", filteredUsers.size());

            // Create a new UserResponseDto with the filtered content
            UserResponseDto response = new UserResponseDto();
            response.setContent(filteredUsers);
            response.setPageNo(userPage.getNumber() + 1);
            response.setPageSize(userPage.getSize());
            response.setTotalElements(filteredUsers.size());  // This is an approximation
            response.setTotalPages(Math.max(1, (int) Math.ceil((double) filteredUsers.size() / pageSize)));
            response.setLast(true);  // This is an approximation
            return response;
        }

        // Create the UserResponseDto using the mapper
        return userMapper.toPageDto(userDtos, userPage);
    }

    // Functional interface for specification creation
    @FunctionalInterface
    private interface SpecificationCreator {
        Specification<UserEntity> createSpec(String username, Status status, RoleEnum role);
    }


    @Override
    public UserDto getUserById(Long id) {
        UserEntity user = userRepository.findUserWithShopById(id)
                .orElseThrow(() -> new NotFoundException(String.format(ErrorMessages.USER_NOT_FOUND, id)));

        // Ensure status is set
        if (user.getStatus() == null) {
            user.setStatus(Status.ACTIVE);
            userRepository.save(user);
        }

        List<UserDto> enrichedUsers = userMapper.enrichUsersWithSubscriptions(List.of(user));
        return enrichedUsers.get(0);
    }

    @Override
    public UserDto getUserByToken() {
        UserEntity currentUser = securityUtils.getCurrentUser();

        // Ensure status is set
        if (currentUser.getStatus() == null) {
            currentUser.setStatus(Status.ACTIVE);
            userRepository.save(currentUser);
        }

        List<UserDto> enrichedUsers = userMapper.enrichUsersWithSubscriptions(List.of(currentUser));
        return enrichedUsers.get(0);
    }

    @Override
    @Transactional
    public UserDto updateUser(Long id, UserUpdateDto updateDto) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format(ErrorMessages.USER_NOT_FOUND, id)));

        // Update fields from DTO if provided
        userMapper.updateUserFromDto(updateDto, user);

        // If role is being updated, fetch the role entity
        if (updateDto.getRole() != null) {
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

        List<UserDto> enrichedUsers = userMapper.enrichUsersWithSubscriptions(List.of(updatedUser));
        return enrichedUsers.get(0);
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