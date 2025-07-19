package com.emenu.features.user_management.service.impl;

import com.emenu.enums.*;
import com.emenu.exception.*;
import com.emenu.features.user_management.domain.Role;
import com.emenu.features.user_management.domain.User;
import com.emenu.features.user_management.dto.filter.PlatformUserFilterRequest;
import com.emenu.features.user_management.dto.request.CreatePlatformUserRequest;
import com.emenu.features.user_management.dto.response.PlatformUserResponse;
import com.emenu.features.user_management.dto.update.UpdatePlatformUserRequest;
import com.emenu.features.user_management.repository.RoleRepository;
import com.emenu.features.user_management.repository.UserRepository;
import com.emenu.features.user_management.service.PlatformUserService;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.utils.pagination.PaginationUtils;
import com.emenu.utils.validation.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PlatformUserServiceImpl implements PlatformUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PlatformUserMapper platformUserMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public PlatformUserResponse createPlatformUser(CreatePlatformUserRequest request) {
        log.info("Creating platform user: {}", request.getEmail());

        // Validate
        ValidationUtils.validateEmail(request.getEmail());
        ValidationUtils.validatePassword(request.getPassword());

        if (userRepository.existsByEmailAndIsDeletedFalse(request.getEmail())) {
            throw new ValidationException("Email already exists");
        }

        // Create user
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setUserType(UserType.PLATFORM_USER);
        user.setStatus(Status.ACTIVE);
        user.setEmailVerified(true); // Platform users are pre-verified

        // Set default platform role
        Role platformRole = roleRepository.findByName(RoleEnum.PLATFORM_SUPPORT)
                .orElseThrow(() -> new ValidationException("Platform role not found"));
        user.setRoles(List.of(platformRole));

        User savedUser = userRepository.save(user);
        log.info("Platform user created: {}", savedUser.getEmail());

        return platformUserMapper.toResponse(savedUser);
    }

    @Override
    public PlatformUserResponse getPlatformUser(UUID id) {
        log.info("Getting platform user: {}", id);

        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("Platform user not found"));

        if (!user.isPlatformUser()) {
            throw new ValidationException("User is not a platform user");
        }

        return platformUserMapper.toResponse(user);
    }

    @Override
    public PlatformUserResponse updatePlatformUser(UUID id, UpdatePlatformUserRequest request) {
        log.info("Updating platform user: {}", id);

        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("Platform user not found"));

        if (!user.isPlatformUser()) {
            throw new ValidationException("User is not a platform user");
        }

        // Update fields
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

        User savedUser = userRepository.save(user);
        log.info("Platform user updated: {}", savedUser.getEmail());

        return platformUserMapper.toResponse(savedUser);
    }

    @Override
    public void deletePlatformUser(UUID id) {
        log.info("Deleting platform user: {}", id);

        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("Platform user not found"));

        if (!user.isPlatformUser()) {
            throw new ValidationException("User is not a platform user");
        }

        // Prevent deleting platform owner
        if (user.hasRole(RoleEnum.PLATFORM_OWNER)) {
            throw new ValidationException("Cannot delete platform owner");
        }

        user.softDelete();
        userRepository.save(user);

        log.info("Platform user deleted: {}", user.getEmail());
    }

    @Override
    public PaginationResponse<PlatformUserResponse> listPlatformUsers(PlatformUserFilterRequest filter) {
        log.info("Listing platform users");

        Specification<User> spec = buildSpecification(filter);
        Pageable pageable = PaginationUtils.createPageable(
                filter.getPageNo(), filter.getPageSize(), filter.getSortBy(), filter.getSortDirection());

        Page<User> userPage = userRepository.findAll(spec, pageable);
        List<PlatformUserResponse> content = userPage.getContent().stream()
                .map(platformUserMapper::toResponse)
                .toList();

        return PaginationResponse.<PlatformUserResponse>builder()
                .content(content)
                .pageNo(userPage.getNumber())
                .pageSize(userPage.getSize())
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .last(userPage.isLast())
                .first(userPage.isFirst())
                .hasNext(userPage.hasNext())
                .hasPrevious(userPage.hasPrevious())
                .build();
    }

    private Specification<User> buildSpecification(PlatformUserFilterRequest filter) {
        return (root, query, cb) -> {
            var predicates = cb.and();

            // Only platform users
            predicates = cb.and(predicates, cb.equal(root.get("userType"), UserType.PLATFORM_USER));

            // Not deleted
            predicates = cb.and(predicates, cb.equal(root.get("isDeleted"), false));

            // Search
            if (filter.getSearch() != null && !filter.getSearch().trim().isEmpty()) {
                String searchPattern = "%" + filter.getSearch().toLowerCase() + "%";
                var searchPredicate = cb.or(
                        cb.like(cb.lower(root.get("firstName")), searchPattern),
                        cb.like(cb.lower(root.get("lastName")), searchPattern),
                        cb.like(cb.lower(root.get("email")), searchPattern)
                );
                predicates = cb.and(predicates, searchPredicate);
            }

            // Status
            if (filter.getStatus() != null) {
                predicates = cb.and(predicates, cb.equal(root.get("status"), filter.getStatus()));
            }

            // Email verified
            if (filter.getEmailVerified() != null) {
                predicates = cb.and(predicates, cb.equal(root.get("emailVerified"), filter.getEmailVerified()));
            }

            // Date filters
            if (filter.getCreatedAfter() != null) {
                predicates = cb.and(predicates, cb.greaterThanOrEqualTo(root.get("createdAt"), filter.getCreatedAfter().atStartOfDay()));
            }
            if (filter.getCreatedBefore() != null) {
                predicates = cb.and(predicates, cb.lessThanOrEqualTo(root.get("createdAt"), filter.getCreatedBefore().atTime(23, 59, 59)));
            }

            return predicates;
        };
    }
}