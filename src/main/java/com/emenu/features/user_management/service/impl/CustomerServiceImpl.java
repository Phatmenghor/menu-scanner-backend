package com.emenu.features.user_management.service.impl;

import com.emenu.enums.CustomerTier;
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
import com.emenu.features.user_management.service.CustomerService;
import com.emenu.features.user_management.service.UserService;
import com.emenu.shared.dto.PaginationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final UserService userService;
    private final UserRepository userRepository;

    @Override
    public UserResponse registerCustomer(UserCreateRequest request) {
        log.info("Registering customer with email: {}", request.getEmail());

        request.setUserType(UserType.CUSTOMER);
        request.setRoles(Arrays.asList(RoleEnum.CUSTOMER));
        
        return userService.createUser(request);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<UserSummaryResponse> getCustomers(UserFilterRequest filter) {
        filter.setUserType(UserType.CUSTOMER);
        return userService.getUsers(filter);
    }

    @Override
    public UserResponse updateCustomer(UUID id, UserUpdateRequest request) {
        log.info("Updating customer with ID: {}", id);

        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));

        if (!user.isCustomer()) {
            throw new ValidationException("User is not a customer");
        }

        return userService.updateUser(id, request);
    }

    @Override
    public void deleteCustomer(UUID id) {
        log.info("Deleting customer with ID: {}", id);

        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));

        if (!user.isCustomer()) {
            throw new ValidationException("User is not a customer");
        }

        userService.deleteUser(id);
    }

    @Override
    public void addLoyaltyPoints(UUID customerId, Integer points) {
        log.info("Adding {} loyalty points to customer ID: {}", points, customerId);

        User customer = userRepository.findByIdAndIsDeletedFalse(customerId)
                .orElseThrow(() -> new UserNotFoundException("Customer not found with ID: " + customerId));

        if (!customer.isCustomer()) {
            throw new ValidationException("User is not a customer");
        }

        customer.setLoyaltyPoints(customer.getLoyaltyPoints() + points);
        
        // Check for tier upgrade
        CustomerTier newTier = CustomerTier.fromPoints(customer.getLoyaltyPoints());
        if (newTier != customer.getCustomerTier()) {
            customer.setCustomerTier(newTier);
            log.info("Customer {} upgraded to tier: {}", customerId, newTier);
        }

        userRepository.save(customer);
        log.info("Loyalty points added successfully to customer ID: {}", customerId);
    }

    @Override
    public void upgradeTier(UUID customerId) {
        log.info("Upgrading tier for customer ID: {}", customerId);

        User customer = userRepository.findByIdAndIsDeletedFalse(customerId)
                .orElseThrow(() -> new UserNotFoundException("Customer not found with ID: " + customerId));

        if (!customer.isCustomer()) {
            throw new ValidationException("User is not a customer");
        }

        CustomerTier newTier = CustomerTier.fromPoints(customer.getLoyaltyPoints());
        customer.setCustomerTier(newTier);
        
        userRepository.save(customer);
        log.info("Customer tier upgraded to: {} for customer ID: {}", newTier, customerId);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCustomerProfile(UUID customerId) {
        User customer = userRepository.findByIdAndIsDeletedFalse(customerId)
                .orElseThrow(() -> new UserNotFoundException("Customer not found with ID: " + customerId));

        if (!customer.isCustomer()) {
            throw new ValidationException("User is not a customer");
        }

        return userService.getUserById(customerId);
    }
}
