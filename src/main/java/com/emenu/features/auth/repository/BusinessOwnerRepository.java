package com.emenu.features.auth.repository;

import com.emenu.enums.payment.PaymentStatus;
import com.emenu.enums.sub_scription.SubscriptionStatus;
import com.emenu.enums.user.AccountStatus;
import com.emenu.enums.user.BusinessStatus;
import com.emenu.features.auth.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BusinessOwnerRepository extends JpaRepository<User, UUID> {

    /**
     * Find business owner by ID with relationships
     */
    @Query("""
        SELECT DISTINCT u FROM User u
        LEFT JOIN FETCH u.business b
        LEFT JOIN FETCH b.subscriptions s
        WHERE u.id = :ownerId 
        AND u.userType = 'BUSINESS_USER'
        AND u.isDeleted = false
    """)
    Optional<User> findBusinessOwnerById(@Param("ownerId") UUID ownerId);

    /**
     * Find all business owners with comprehensive filtering
     */
    @Query("""
        SELECT DISTINCT u FROM User u
        LEFT JOIN u.business b
        LEFT JOIN b.subscriptions s
        LEFT JOIN s.payments p
        WHERE u.userType = 'BUSINESS_USER'
        AND u.isDeleted = false
        AND b.isDeleted = false
        
        -- Owner account status filter
        AND (:ownerAccountStatuses IS NULL OR u.accountStatus IN :ownerAccountStatuses)
        
        -- Business status filter
        AND (:businessStatuses IS NULL OR b.status IN :businessStatuses)
        
        -- Subscription status filter
        AND (
            :subscriptionStatuses IS NULL 
            OR (
                :hasActive = true AND s.endDate > :now
            )
            OR (
                :hasExpired = true AND s.endDate <= :now
            )
            OR (
                :hasExpiringSoon = true AND s.endDate > :now AND s.endDate <= :expiryThreshold
            )
        )
        
        -- Auto-renew filter
        AND (:autoRenew IS NULL OR s.autoRenew = :autoRenew)
        
        -- Payment status filter
        AND (:paymentStatuses IS NULL OR p.status IN :paymentStatuses)
        
        -- Search filter
        AND (:search IS NULL OR :search = '' OR
             LOWER(u.userIdentifier) LIKE LOWER(CONCAT('%', :search, '%')) OR
             LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR
             LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR
             LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR
             LOWER(b.name) LIKE LOWER(CONCAT('%', :search, '%')) OR
             LOWER(b.email) LIKE LOWER(CONCAT('%', :search, '%')))
        
        ORDER BY u.createdAt DESC
    """)
    Page<User> findAllBusinessOwnersWithFilters(
            @Param("ownerAccountStatuses") List<AccountStatus> ownerAccountStatuses,
            @Param("businessStatuses") List<BusinessStatus> businessStatuses,
            @Param("subscriptionStatuses") List<SubscriptionStatus> subscriptionStatuses,
            @Param("hasActive") boolean hasActive,
            @Param("hasExpired") boolean hasExpired,
            @Param("hasExpiringSoon") boolean hasExpiringSoon,
            @Param("now") LocalDateTime now,
            @Param("expiryThreshold") LocalDateTime expiryThreshold,
            @Param("autoRenew") Boolean autoRenew,
            @Param("paymentStatuses") List<PaymentStatus> paymentStatuses,
            @Param("search") String search,
            Pageable pageable
    );

    /**
     * Check if business owner exists by email
     */
    @Query("""
        SELECT COUNT(u) > 0 FROM User u
        WHERE u.email = :email
        AND u.userType = 'BUSINESS_USER'
        AND u.isDeleted = false
    """)
    boolean existsBusinessOwnerByEmail(@Param("email") String email);
}