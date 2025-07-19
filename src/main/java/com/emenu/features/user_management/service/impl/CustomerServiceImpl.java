package com.emenu.features.user_management.service.impl;

import com.emenu.enums.CustomerTier;
import com.emenu.enums.RoleEnum;
import com.emenu.enums.Status;
import com.emenu.enums.UserType;
import com.emenu.exception.UserNotFoundException;
import com.emenu.exception.ValidationException;
import com.emenu.features.user_management.domain.Role;
import com.emenu.features.user_management.domain.User;
import com.emenu.features.user_management.dto.filter.CustomerFilterRequest;
import com.emenu.features.user_management.dto.request.CreateCustomerRequest;
import com.emenu.features.user_management.dto.response.CustomerResponse;
import com.emenu.features.user_management.dto.update.UpdateCustomerRequest;
import com.emenu.features.user_management.mapper.CustomerMapper;
import com.emenu.features.user_management.repository.RoleRepository;
import com.emenu.features.user_management.repository.UserRepository;
import com.emenu.features.user_management.service.CustomerService;
import com.emenu.security.SecurityUtils;
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
public class CustomerServiceImpl implements CustomerService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CustomerMapper customerMapper;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtils securityUtils;

    @Override
    public CustomerResponse createCustomer(CreateCustomerRequest request) {
        log.info("Creating customer: {}", request.getEmail());

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
        user.setUserType(UserType.CUSTOMER);
        user.setStatus(Status.PENDING);
        user.setEmailVerified(false);
        user.setLoyaltyPoints(0);
        user.setCustomerTier(CustomerTier.BRONZE);

        // Generate verification token
        user.setEmailVerificationToken(UUID.randomUUID().toString());
        user.setEmailVerificationExpires(java.time.LocalDateTime.now().plusDays(1));

        // Set customer role
        Role customerRole = roleRepository.findByName(RoleEnum.CUSTOMER)
                .orElseThrow(() -> new ValidationException("Customer role not found"));
        user.setRoles(List.of(customerRole));

        User savedUser = userRepository.save(user);
        log.info("Customer created: {}", savedUser.getEmail());

        return customerMapper.toResponse(savedUser);
    }

    @Override
    public CustomerResponse getCustomer(UUID id) {
        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("Customer not found"));

        if (!user.isCustomer()) {
            throw new ValidationException("User is not a customer");
        }

        return customerMapper.toResponse(user);
    }

    @Override
    public CustomerResponse updateCustomer(UUID id, UpdateCustomerRequest request) {
        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("Customer not found"));

        if (!user.isCustomer()) {
            throw new ValidationException("User is not a customer");
        }

        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());

        User savedUser = userRepository.save(user);
        return customerMapper.toResponse(savedUser);
    }

    @Override
    public void addLoyaltyPoints(UUID id, Integer points) {
        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("Customer not found"));

        if (!user.isCustomer()) {
            throw new ValidationException("User is not a customer");
        }

        user.addLoyaltyPoints(points);
        userRepository.save(user);

        log.info("Added {} loyalty points to customer: {}", points, user.getEmail());
    }

    @Override
    public CustomerResponse getCurrentCustomer() {
        User currentUser = securityUtils.getCurrentUser();
        
        if (!currentUser.isCustomer()) {
            throw new ValidationException("Current user is not a customer");
        }

        return customerMapper.toResponse(currentUser);
    }

    @Override
    public PaginationResponse<CustomerResponse> listCustomers(CustomerFilterRequest filter) {
        Specification<User> spec = buildCustomerSpec(filter);
        Pageable pageable = PaginationUtils.createPageable(
                filter.getPageNo(), filter.getPageSize(), filter.getSortBy(), filter.getSortDirection());

        Page<User> userPage = userRepository.findAll(spec, pageable);
        List<CustomerResponse> content = userPage.getContent().stream()
                .map(customerMapper::toResponse)
                .toList();

        return PaginationResponse.<CustomerResponse>builder()
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

    private Specification<User> buildCustomerSpec(CustomerFilterRequest filter) {
        return (root, query, cb) -> {
            var predicates = cb.and();
            predicates = cb.and(predicates, cb.equal(root.get("userType"), UserType.CUSTOMER));
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

            if (filter.getCustomerTier() != null) {
                predicates = cb.and(predicates, cb.equal(root.get("customerTier"), CustomerTier.valueOf(filter.getCustomerTier())));
            }

            if (filter.getMinLoyaltyPoints() != null) {
                predicates = cb.and(predicates, cb.greaterThanOrEqualTo(root.get("loyaltyPoints"), filter.getMinLoyaltyPoints()));
            }

            if (filter.getMaxLoyaltyPoints() != null) {
                predicates = cb.and(predicates, cb.lessThanOrEqualTo(root.get("loyaltyPoints"), filter.getMaxLoyaltyPoints()));
            }

            return predicates;
        };
    }
}