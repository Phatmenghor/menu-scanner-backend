package com.emenu.features.user_management.service.impl;

import com.emenu.enums.Status;
import com.emenu.enums.UserType;
import com.emenu.exception.UserNotFoundException;
import com.emenu.exception.ValidationException;
import com.emenu.features.user_management.domain.Role;
import com.emenu.features.user_management.domain.User;
import com.emenu.features.user_management.dto.filter.BusinessUserFilterRequest;
import com.emenu.features.user_management.dto.request.CreateBusinessUserRequest;
import com.emenu.features.user_management.dto.response.BusinessUserResponse;
import com.emenu.features.user_management.dto.update.UpdateBusinessUserRequest;
import com.emenu.features.user_management.repository.RoleRepository;
import com.emenu.features.user_management.repository.UserRepository;
import com.emenu.features.user_management.service.BusinessUserService;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.utils.pagination.PaginationUtils;
import com.emenu.utils.validation.ValidationUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BusinessUserServiceImpl implements BusinessUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BusinessUserMapper businessUserMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public BusinessUserResponse createBusinessUser(CreateBusinessUserRequest request) {
        log.info("Creating business user: {}", request.getEmail());

        ValidationUtils.validateEmail(request.getEmail());
        ValidationUtils.validatePassword(request.getPassword());

        if (userRepository.existsByEmailAndIsDeletedFalse(request.getEmail())) {
            throw new ValidationException("Email already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setUserType(UserType.BUSINESS_USER);
        user.setStatus(Status.PENDING);
        user.setEmailVerified(false);
        user.setBusinessId(request.getBusinessId());

        // Generate verification token
        user.setEmailVerificationToken(UUID.randomUUID().toString());
        user.setEmailVerificationExpires(java.time.LocalDateTime.now().plusDays(1));

        // Set role
        Role role = roleRepository.findByName(request.getRole())
                .orElseThrow(() -> new ValidationException("Role not found"));
        user.setRoles(List.of(role));

        User savedUser = userRepository.save(user);
        log.info("Business user created: {}", savedUser.getEmail());

        return businessUserMapper.toResponse(savedUser);
    }

    @Override
    public BusinessUserResponse getBusinessUser(UUID id) {
        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("Business user not found"));

        if (!user.isBusinessUser()) {
            throw new ValidationException("User is not a business user");
        }

        return businessUserMapper.toResponse(user);
    }

    @Override
    public BusinessUserResponse updateBusinessUser(UUID id, UpdateBusinessUserRequest request) {
        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("Business user not found"));

        if (!user.isBusinessUser()) {
            throw new ValidationException("User is not a business user");
        }

        // Update fields
        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());
        if (request.getBusinessId() != null) user.setBusinessId(request.getBusinessId());

        // Update role
        if (request.getRole() != null) {
            Role role = roleRepository.findByName(request.getRole())
                    .orElseThrow(() -> new ValidationException("Role not found"));
            user.setRoles(List.of(role));
        }

        User savedUser = userRepository.save(user);
        return businessUserMapper.toResponse(savedUser);
    }

    @Override
    public void deleteBusinessUser(UUID id) {
        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("Business user not found"));

        if (!user.isBusinessUser()) {
            throw new ValidationException("User is not a business user");
        }

        user.softDelete();
        userRepository.save(user);
        log.info("Business user deleted: {}", user.getEmail());
    }

    @Override
    public void changePassword(UUID id, String newPassword) {
        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("Business user not found"));

        ValidationUtils.validatePassword(newPassword);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("Password changed for business user: {}", user.getEmail());
    }

    @Override
    public PaginationResponse<BusinessUserResponse> listBusinessUsers(BusinessUserFilterRequest filter) {
        Specification<User> spec = buildBusinessUserSpec(filter);
        Pageable pageable = PaginationUtils.createPageable(
                filter.getPageNo(), filter.getPageSize(), filter.getSortBy(), filter.getSortDirection());

        Page<User> userPage = userRepository.findAll(spec, pageable);
        List<BusinessUserResponse> content = userPage.getContent().stream()
                .map(businessUserMapper::toResponse)
                .toList();

        return PaginationResponse.<BusinessUserResponse>builder()
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

    private Specification<User> buildBusinessUserSpec(BusinessUserFilterRequest filter) {
        return (root, query, cb) -> {
            var predicates = cb.and();
            predicates = cb.and(predicates, cb.equal(root.get("userType"), UserType.BUSINESS_USER));
            predicates = cb.and(predicates, cb.equal(root.get("isDeleted"), false));

            if (filter.getSearch() != null && !filter.getSearch().trim().isEmpty()) {
                String searchPattern = "%" + filter.getSearch().toLowerCase() + "%";
                var searchPredicate = cb.or(
                        cb.like(cb.lower(root.get("firstName")), searchPattern),
                        cb.like(cb.lower(root.get("lastName")), searchPattern),
                        cb.like(cb.lower(root.get("email")), searchPattern)
                );
                predicates = cb.and(predicates, searchPredicate);
            }

            if (filter.getStatus() != null) {
                predicates = cb.and(predicates, cb.equal(root.get("status"), filter.getStatus()));
            }

            if (filter.getBusinessId() != null) {
                predicates = cb.and(predicates, cb.equal(root.get("businessId"), filter.getBusinessId()));
            }

            return predicates;
        };
    }
}