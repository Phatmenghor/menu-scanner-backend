package com.emenu.features.user_management.service.impl;

import com.emenu.enums.RoleEnum;
import com.emenu.enums.UserType;
import com.emenu.exception.UserNotFoundException;
import com.emenu.exception.ValidationException;
import com.emenu.features.user_management.dto.filter.UserFilterRequest;
import com.emenu.features.user_management.dto.request.UserCreateRequest;
import com.emenu.features.user_management.dto.response.UserResponse;
import com.emenu.features.user_management.dto.response.UserSummaryResponse;
import com.emenu.features.user_management.dto.update.UserUpdateRequest;
import com.emenu.features.user_management.models.User;
import com.emenu.features.user_management.repository.UserRepository;
import com.emenu.features.user_management.service.BusinessUserService;
import com.emenu.features.user_management.service.UserService;
import com.emenu.security.SecurityUtils;
import com.emenu.shared.dto.PaginationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BusinessUserServiceImpl implements BusinessUserService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;

    @Override
    public UserResponse createBusinessUser(UserCreateRequest request) {
        log.info("Creating business user with email: {}", request.getEmail());

        validateBusinessUserRequest(request);
        request.setUserType(UserType.BUSINESS_USER);
        
        return userService.createUser(request);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<UserSummaryResponse> getBusinessUsers(UUID businessId, UserFilterRequest filter) {
        filter.setUserType(UserType.BUSINESS_USER);
        filter.setBusinessId(businessId);
        return userService.getUsers(filter);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<UserSummaryResponse> getMyBusinessUsers(UserFilterRequest filter) {
        User currentUser = securityUtils.getCurrentUser();
        
        if (currentUser.getBusinessId() == null) {
            throw new ValidationException("Current user is not associated with any business");
        }
        
        return getBusinessUsers(currentUser.getBusinessId(), filter);
    }

    @Override
    public UserResponse updateBusinessUser(UUID id, UserUpdateRequest request) {
        log.info("Updating business user with ID: {}", id);

        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));

        if (!user.isBusinessUser()) {
            throw new ValidationException("User is not a business user");
        }

        return userService.updateUser(id, request);
    }

    @Override
    public void deleteBusinessUser(UUID id) {
        log.info("Deleting business user with ID: {}", id);

        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));

        if (!user.isBusinessUser()) {
            throw new ValidationException("User is not a business user");
        }

        userService.deleteUser(id);
    }

    @Override
    public UserResponse createStaffMember(UserCreateRequest request) {
        log.info("Creating staff member with email: {}", request.getEmail());

        User currentUser = securityUtils.getCurrentUser();
        
        if (currentUser.getBusinessId() == null) {
            throw new ValidationException("Current user is not associated with any business");
        }

        request.setBusinessId(currentUser.getBusinessId());
        request.setRoles(Arrays.asList(RoleEnum.BUSINESS_STAFF));
        
        return createBusinessUser(request);
    }

    @Override
    @Transactional(readOnly = true)
    public long getBusinessUsersCount(UUID businessId) {
        return userRepository.countByBusinessId(businessId);
    }

    private void validateBusinessUserRequest(UserCreateRequest request) {
        if (request.getRoles() == null || request.getRoles().isEmpty()) {
            throw new ValidationException("Business user must have at least one role");
        }

        List<RoleEnum> businessRoles = Arrays.asList(
                RoleEnum.BUSINESS_OWNER,
                RoleEnum.BUSINESS_MANAGER,
                RoleEnum.BUSINESS_STAFF
        );

        boolean hasValidRole = request.getRoles().stream()
                .anyMatch(businessRoles::contains);

        if (!hasValidRole) {
            throw new ValidationException("Invalid role for business user");
        }

        // Business owner and manager should have business ID
        if (request.getRoles().contains(RoleEnum.BUSINESS_OWNER) || 
            request.getRoles().contains(RoleEnum.BUSINESS_MANAGER)) {
            if (request.getBusinessId() == null) {
                throw new ValidationException("Business ID is required for business owner/manager");
            }
        }
    }
}
