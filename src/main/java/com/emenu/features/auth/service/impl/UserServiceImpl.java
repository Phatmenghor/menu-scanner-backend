package com.emenu.features.auth.service.impl;

import com.emenu.enums.AccountStatus;
import com.emenu.exception.UserNotFoundException;
import com.emenu.exception.ValidationException;
import com.emenu.features.auth.dto.filter.UserFilterRequest;
import com.emenu.features.auth.dto.request.UserCreateRequest;
import com.emenu.features.auth.dto.response.UserResponse;
import com.emenu.features.auth.dto.update.UserUpdateRequest;
import com.emenu.features.auth.mapper.UserMapper;
import com.emenu.features.auth.models.Role;
import com.emenu.features.auth.models.User;
import com.emenu.features.auth.repository.RoleRepository;
import com.emenu.features.auth.repository.UserRepository;
import com.emenu.features.auth.service.UserService;
import com.emenu.features.auth.specification.UserSpecification;
import com.emenu.security.SecurityUtils;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.pagination.PaginationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

    // ✅ Clean Dependencies - Only repositories, mappers, and utils
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtils securityUtils;

    @Override
    public UserResponse createUser(UserCreateRequest request) {
        log.info("Creating user: {}", request.getEmail());

        // Validation
        if (userRepository.existsByEmailAndIsDeletedFalse(request.getEmail())) {
            throw new ValidationException("Email already exists");
        }

        // ✅ Mapper handles entity creation
        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Set roles
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            List<Role> roles = roleRepository.findByNameIn(request.getRoles());
            user.setRoles(roles);
        }

        User savedUser = userRepository.save(user);
        log.info("User created successfully: {}", savedUser.getEmail());

        // ✅ Mapper handles response conversion
        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<UserResponse> getUsers(UserFilterRequest filter) {
        // ✅ Specification handles all filtering logic
        Specification<User> spec = UserSpecification.buildSpecification(filter);
        
        // ✅ Utility handles pagination creation
        int pageNo = filter.getPageNo() != null && filter.getPageNo() > 0 ? filter.getPageNo() - 1 : 0;
        Pageable pageable = PaginationUtils.createPageable(
                pageNo, filter.getPageSize(), filter.getSortBy(), filter.getSortDirection()
        );

        Page<User> userPage = userRepository.findAll(spec, pageable);

        // ✅ Mapper handles pagination response conversion
        return userMapper.toPaginationResponse(userPage);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {
        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return userMapper.toResponse(user);
    }

    @Override
    public UserResponse updateUser(UUID id, UserUpdateRequest request) {
        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // ✅ Mapper handles entity updates
        userMapper.updateEntity(request, user);

        // Handle roles update
        if (request.getRoles() != null) {
            List<Role> roles = roleRepository.findByNameIn(request.getRoles());
            user.setRoles(roles);
        }

        User updatedUser = userRepository.save(user);
        log.info("User updated successfully: {}", updatedUser.getEmail());

        return userMapper.toResponse(updatedUser);
    }

    @Override
    public void deleteUser(UUID id) {
        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.softDelete();
        userRepository.save(user);
        log.info("User deleted successfully: {}", user.getEmail());
    }

    @Override
    public void activateUser(UUID id) {
        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setAccountStatus(AccountStatus.ACTIVE);
        userRepository.save(user);
        log.info("User activated successfully: {}", user.getEmail());
    }

    @Override
    public void deactivateUser(UUID id) {
        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setAccountStatus(AccountStatus.INACTIVE);
        userRepository.save(user);
        log.info("User deactivated successfully: {}", user.getEmail());
    }

    @Override
    public void lockUser(UUID id) {
        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setAccountStatus(AccountStatus.LOCKED);
        userRepository.save(user);
        log.info("User locked successfully: {}", user.getEmail());
    }

    @Override
    public void unlockUser(UUID id) {
        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setAccountStatus(AccountStatus.ACTIVE);
        userRepository.save(user);
        log.info("User unlocked successfully: {}", user.getEmail());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUserProfile() {
        User currentUser = securityUtils.getCurrentUser();
        return userMapper.toResponse(currentUser);
    }

    @Override
    public UserResponse updateCurrentUserProfile(UserUpdateRequest request) {
        User currentUser = securityUtils.getCurrentUser();
        
        userMapper.updateEntity(request, currentUser);
        User updatedUser = userRepository.save(currentUser);
        
        log.info("Profile updated for user: {}", currentUser.getEmail());
        return userMapper.toResponse(updatedUser);
    }
}