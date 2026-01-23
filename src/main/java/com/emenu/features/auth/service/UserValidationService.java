package com.emenu.features.auth.service;

import com.emenu.enums.user.UserType;
import com.emenu.features.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service for validating user-related business rules.
 * Handles dynamic username uniqueness based on user type.
 *
 * @author Cambodia E-Menu Platform
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserValidationService {

    private final UserRepository userRepository;

    /**
     * Check if a username is available based on user type and business context.
     *
     * Dynamic uniqueness rules:
     * - PLATFORM_USER: Username must be globally unique among platform users
     * - CUSTOMER: Username must be globally unique among customers
     * - BUSINESS_USER: Username must be unique within the specific business (businessId)
     *
     * @param userIdentifier the username to check
     * @param userType the user type
     * @param businessId the business ID (required for BUSINESS_USER, nullable for others)
     * @return true if username is available, false otherwise
     */
    public boolean isUsernameAvailable(String userIdentifier, UserType userType, UUID businessId) {
        log.debug("Checking username availability: {} for type: {} in business: {}", userIdentifier, userType, businessId);

        switch (userType) {
            case PLATFORM_USER:
            case CUSTOMER:
                // For platform users and customers, check global uniqueness within their user type
                boolean existsByType = userRepository.existsByUserIdentifierAndUserTypeAndIsDeletedFalse(userIdentifier, userType);
                log.debug("Username {} exists for type {}: {}", userIdentifier, userType, existsByType);
                return !existsByType;

            case BUSINESS_USER:
                // For business users, check uniqueness within the specific business
                if (businessId == null) {
                    log.warn("Business ID is required for BUSINESS_USER type validation");
                    throw new IllegalArgumentException("Business ID is required for BUSINESS_USER type");
                }
                boolean existsInBusiness = userRepository.existsByUserIdentifierAndBusinessIdAndIsDeletedFalse(userIdentifier, businessId);
                log.debug("Username {} exists in business {}: {}", userIdentifier, businessId, existsInBusiness);
                return !existsInBusiness;

            default:
                log.error("Unknown user type: {}", userType);
                throw new IllegalArgumentException("Unknown user type: " + userType);
        }
    }

    /**
     * Validate username uniqueness and throw exception if not available.
     *
     * @param userIdentifier the username to validate
     * @param userType the user type
     * @param businessId the business ID (required for BUSINESS_USER)
     * @throws com.emenu.exception.custom.ValidationException if username is not available
     */
    public void validateUsernameUniqueness(String userIdentifier, UserType userType, UUID businessId) {
        if (!isUsernameAvailable(userIdentifier, userType, businessId)) {
            String context = userType == UserType.BUSINESS_USER
                    ? " in this business"
                    : " for " + userType.name().toLowerCase().replace("_", " ");
            throw new com.emenu.exception.custom.ValidationException(
                    "Username '" + userIdentifier + "' is already taken" + context
            );
        }
    }
}
