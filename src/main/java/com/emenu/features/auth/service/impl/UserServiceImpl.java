package com.emenu.features.auth.service.impl;

import com.emenu.enums.payment.PaymentType;
import com.emenu.enums.user.AccountStatus;
import com.emenu.enums.user.RoleEnum;
import com.emenu.enums.user.UserType;
import com.emenu.exception.custom.ValidationException;
import com.emenu.features.auth.dto.filter.UserFilterRequest;
import com.emenu.features.auth.dto.request.BusinessCreateRequest;
import com.emenu.features.auth.dto.request.BusinessOwnerCreateRequest;
import com.emenu.features.auth.dto.request.UserCreateRequest;
import com.emenu.features.auth.dto.response.BusinessOwnerCreateResponse;
import com.emenu.features.auth.dto.response.BusinessResponse;
import com.emenu.features.auth.dto.response.UserResponse;
import com.emenu.features.auth.dto.update.UserUpdateRequest;
import com.emenu.features.auth.mapper.BusinessOwnerResponseMapper;
import com.emenu.features.auth.mapper.UserMapper;
import com.emenu.features.auth.models.Business;
import com.emenu.features.auth.models.Role;
import com.emenu.features.auth.models.User;
import com.emenu.features.auth.repository.BusinessRepository;
import com.emenu.features.auth.repository.RoleRepository;
import com.emenu.features.auth.repository.UserRepository;
import com.emenu.features.auth.service.BusinessService;
import com.emenu.features.auth.service.UserService;
import com.emenu.features.payment.dto.request.PaymentCreateRequest;
import com.emenu.features.payment.dto.response.PaymentResponse;
import com.emenu.features.payment.service.PaymentService;
import com.emenu.features.subscription.dto.request.SubscriptionCreateRequest;
import com.emenu.features.subscription.dto.response.SubscriptionResponse;
import com.emenu.features.subscription.repository.SubscriptionPlanRepository;
import com.emenu.features.subscription.service.SubscriptionService;
import com.emenu.security.SecurityUtils;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.pagination.PaginationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BusinessRepository businessRepository;
    private final BusinessService businessService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtils securityUtils;
    private final PaymentService paymentService;
    private final SubscriptionService subscriptionService;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final BusinessOwnerResponseMapper businessOwnerResponseMapper;

    @Override
    public UserResponse createUser(UserCreateRequest request) {
        log.info("Creating user: {}", request.getUserIdentifier());

        if (userRepository.existsByUserIdentifierAndIsDeletedFalse(request.getUserIdentifier())) {
            throw new ValidationException("User identifier already exists");
        }

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        if (request.getBusinessId() != null) {
            Business business = businessRepository.findByIdAndIsDeletedFalse(request.getBusinessId())
                    .orElseThrow(() -> new ValidationException("Business not found"));
            user.setBusinessId(business.getId());
        }

        List<Role> roles = roleRepository.findByNameIn(request.getRoles());
        if (roles.size() != request.getRoles().size()) {
            throw new ValidationException("One or more roles not found");
        }
        user.setRoles(roles);

        User savedUser = userRepository.save(user);
        log.info("User created: {}", savedUser.getUserIdentifier());
        
        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<UserResponse> getAllUsers(UserFilterRequest request) {
        User currentUser = securityUtils.getCurrentUser();
        if (currentUser.isBusinessUser() && request.getBusinessId() == null) {
            request.setBusinessId(currentUser.getBusinessId());
        }

        Pageable pageable = PaginationUtils.createPageable(
                request.getPageNo(), request.getPageSize(), request.getSortBy(), request.getSortDirection()
        );

        // Convert empty lists to null to skip filtering
        List<UserType> userTypes = (request.getUserTypes() != null && !request.getUserTypes().isEmpty())
                ? request.getUserTypes() : null;
        List<AccountStatus> accountStatuses = (request.getAccountStatuses() != null && !request.getAccountStatuses().isEmpty())
                ? request.getAccountStatuses() : null;
        List<RoleEnum> roles = (request.getRoles() != null && !request.getRoles().isEmpty())
                ? request.getRoles() : null;

        Page<User> userPage = userRepository.searchUsers(
                request.getBusinessId(),
                userTypes,
                accountStatuses,
                roles,
                request.getSearch(),
                pageable
        );

        return userMapper.toPaginationResponse(userPage);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID userId) {
        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return userMapper.toResponse(user);
    }

    @Override
    public UserResponse updateUser(UUID userId, UserUpdateRequest request) {
        log.info("Updating user: {}", userId);

        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getBusinessId() != null && !request.getBusinessId().equals(user.getBusinessId())) {
            Business business = businessRepository.findByIdAndIsDeletedFalse(request.getBusinessId())
                    .orElseThrow(() -> new ValidationException("Business not found"));
            user.setBusinessId(business.getId());
        }

        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            List<Role> roles = roleRepository.findByNameIn(request.getRoles());
            if (roles.size() != request.getRoles().size()) {
                throw new ValidationException("One or more roles not found");
            }

            user.getRoles().clear();
            user.getRoles().addAll(roles);
        }

        userMapper.updateEntity(request, user);
        User updatedUser = userRepository.save(user);

        log.info("User updated: {}", updatedUser.getUserIdentifier());
        return userMapper.toResponse(updatedUser);
    }

    @Override
    public UserResponse deleteUser(UUID userId) {
        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        User currentUser = securityUtils.getCurrentUser();
        if (user.getId().equals(currentUser.getId())) {
            throw new ValidationException("You cannot delete your own account");
        }

        user.softDelete();
        user = userRepository.save(user);
        log.info("User deleted: {}", user.getUserIdentifier());

        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        User currentUser = securityUtils.getCurrentUser();
        return userMapper.toResponse(currentUser);
    }

    @Override
    public UserResponse updateCurrentUser(UserUpdateRequest request) {
        User currentUser = securityUtils.getCurrentUser();
        userMapper.updateEntity(request, currentUser);
        User updatedUser = userRepository.save(currentUser);
        
        log.info("Current user updated: {}", updatedUser.getUserIdentifier());
        return userMapper.toResponse(updatedUser);
    }

    @Override
    public BusinessOwnerCreateResponse createBusinessOwner(BusinessOwnerCreateRequest request) {
        log.info("Creating business owner: {}", request.getBusinessName());

        validateBusinessOwnerCreation(request);

        try {
            BusinessCreateRequest businessRequest = new BusinessCreateRequest();
            businessRequest.setName(request.getBusinessName());
            businessRequest.setEmail(request.getBusinessEmail());
            businessRequest.setPhone(request.getBusinessPhone());
            businessRequest.setAddress(request.getBusinessAddress());
            businessRequest.setDescription(request.getBusinessDescription());
            BusinessResponse businessResponse = businessService.createBusiness(businessRequest);

            User user = userMapper.toEntity(request);
            user.setUserType(UserType.BUSINESS_USER);
            user.setAccountStatus(AccountStatus.ACTIVE);
            user.setPassword(passwordEncoder.encode(request.getOwnerPassword()));
            user.setBusinessId(businessResponse.getId());
            
            Role businessOwnerRole = roleRepository.findByName(RoleEnum.BUSINESS_OWNER)
                    .orElseThrow(() -> new ValidationException("Business owner role not found"));
            user.setRoles(List.of(businessOwnerRole));
            
            User savedUser = userRepository.save(user);
            UserResponse userResponse = userMapper.toResponse(savedUser);

            SubscriptionCreateRequest subscriptionRequest = new SubscriptionCreateRequest();
            subscriptionRequest.setBusinessId(businessResponse.getId());
            subscriptionRequest.setPlanId(request.getSubscriptionPlanId());
            subscriptionRequest.setStartDate(request.getSubscriptionStartDate());
            subscriptionRequest.setAutoRenew(request.getAutoRenew());
            SubscriptionResponse subscriptionResponse = subscriptionService.createSubscription(subscriptionRequest);

            PaymentResponse paymentResponse = null;
            if (request.hasPaymentInfo() && request.isPaymentInfoComplete()) {
                PaymentCreateRequest paymentRequest = new PaymentCreateRequest();
                paymentRequest.setImageUrl(request.getPaymentImageUrl());
                paymentRequest.setAmount(request.getPaymentAmount());
                paymentRequest.setPaymentMethod(request.getPaymentMethod());
                paymentRequest.setStatus(request.getPaymentStatus());
                paymentRequest.setReferenceNumber(request.getPaymentReferenceNumber());
                paymentRequest.setNotes(request.getPaymentNotes());
                paymentRequest.setSubscriptionId(subscriptionResponse.getId());
                paymentRequest.setPaymentType(PaymentType.SUBSCRIPTION);
                
                paymentResponse = paymentService.createPayment(paymentRequest);
            }

            BusinessOwnerCreateResponse response = businessOwnerResponseMapper.create(
                    userResponse, businessResponse, subscriptionResponse, paymentResponse
            );

            log.info("Business owner created: {}", userResponse.getUserIdentifier());
            return response;

        } catch (Exception e) {
            log.error("Failed to create business owner: {}", e.getMessage());
            throw new RuntimeException("Failed to create business owner: " + e.getMessage());
        }
    }

    private void validateBusinessOwnerCreation(BusinessOwnerCreateRequest request) {
        if (userRepository.existsByUserIdentifierAndIsDeletedFalse(request.getOwnerUserIdentifier())) {
            throw new ValidationException("User identifier already exists");
        }

        if (!subscriptionPlanRepository.existsById(request.getSubscriptionPlanId())) {
            throw new ValidationException("Subscription plan not found");
        }

        if (request.hasPaymentInfo() && !request.isPaymentInfoComplete()) {
            throw new ValidationException("Payment method required when amount provided");
        }
    }
}