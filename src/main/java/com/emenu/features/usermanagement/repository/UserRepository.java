package com.emenu.features.usermanagement.repository;

import com.emenu.enums.*;
import com.emenu.features.usermanagement.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    // Basic queries
    Optional<User> findByEmailAndIsDeletedFalse(String email);
    Optional<User> findByIdAndIsDeletedFalse(UUID id);
    Optional<User> findByPhoneNumberAndIsDeletedFalse(String phoneNumber);
    Optional<User> findByEmployeeIdAndIsDeletedFalse(String employeeId);

    boolean existsByEmailAndIsDeletedFalse(String email);
    boolean existsByPhoneNumberAndIsDeletedFalse(String phoneNumber);
    boolean existsByEmployeeIdAndIsDeletedFalse(String employeeId);

    // Authentication tokens
    Optional<User> findByEmailVerificationToken(String token);
    Optional<User> findByPhoneVerificationToken(String token);
    Optional<User> findByPasswordResetToken(String token);

    // Pagination
    Page<User> findByIsDeletedFalse(Pageable pageable);
    Page<User> findByUserTypeAndIsDeletedFalse(UserType userType, Pageable pageable);
    Page<User> findByAccountStatusAndIsDeletedFalse(AccountStatus status, Pageable pageable);

    // Business relationships
    List<User> findByBusinessIdAndIsDeletedFalse(UUID businessId);
    List<User> findByPrimaryBusinessIdAndIsDeletedFalse(UUID businessId);

    @Query("SELECT u FROM User u WHERE :businessId MEMBER OF u.accessibleBusinessIds AND u.isDeleted = false")
    List<User> findByAccessibleBusinessId(@Param("businessId") UUID businessId);

    // Role-based queries
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :role AND u.isDeleted = false")
    List<User> findByRoleAndIsDeletedFalse(@Param("role") RoleEnum role);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name IN :roles AND u.isDeleted = false")
    List<User> findByRolesInAndIsDeletedFalse(@Param("roles") List<RoleEnum> roles);

    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r.name IN :roles AND u.isDeleted = false")
    Page<User> findByRolesInAndIsDeletedFalse(@Param("roles") List<RoleEnum> roles, Pageable pageable);

    // Platform users
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name LIKE 'PLATFORM_%' AND u.isDeleted = false")
    List<User> findPlatformUsers();

    @Query("SELECT u FROM User u WHERE u.userType = 'PLATFORM_USER' AND u.isDeleted = false")
    List<User> findByUserTypePlatform();

    @Query("SELECT u FROM User u WHERE u.department = :department AND u.isDeleted = false")
    List<User> findByDepartment(@Param("department") String department);

    // Business users
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name LIKE 'BUSINESS_%' AND u.isDeleted = false")
    List<User> findBusinessUsers();

    @Query("SELECT u FROM User u WHERE u.userType = 'BUSINESS_USER' AND u.isDeleted = false")
    List<User> findByUserTypeBusiness();

    // Customer users
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name LIKE '%CUSTOMER%' AND u.isDeleted = false")
    List<User> findCustomerUsers();

    @Query("SELECT u FROM User u WHERE u.customerTier = :tier AND u.isDeleted = false")
    List<User> findByCustomerTier(@Param("tier") CustomerTier tier);

    @Query("SELECT u FROM User u WHERE u.loyaltyPoints BETWEEN :min AND :max AND u.isDeleted = false")
    List<User> findByLoyaltyPointsBetween(@Param("min") int min, @Param("max") int max);

    // Subscription queries
    @Query("SELECT u FROM User u WHERE u.subscriptionPlan = :plan AND u.isDeleted = false")
    List<User> findBySubscriptionPlan(@Param("plan") SubscriptionPlan plan);

    @Query("SELECT u FROM User u WHERE u.subscriptionEnds BETWEEN :start AND :end AND u.isDeleted = false")
    List<User> findBySubscriptionEndsBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT u FROM User u WHERE u.subscriptionEnds < :now AND u.subscriptionPlan != 'FREE' AND u.isDeleted = false")
    List<User> findExpiredSubscriptions(@Param("now") LocalDateTime now);

    @Query("SELECT u FROM User u WHERE u.subscriptionEnds BETWEEN :now AND :soon AND u.isDeleted = false")
    List<User> findSubscriptionsExpiringSoon(@Param("now") LocalDateTime now, @Param("soon") LocalDateTime soon);

    // Activity queries
    @Query("SELECT u FROM User u WHERE u.accountStatus = :status AND u.isDeleted = false")
    List<User> findByAccountStatusAndIsDeletedFalse(@Param("status") AccountStatus status);

    @Query("SELECT u FROM User u WHERE u.accountLockedUntil < :now AND u.accountStatus = 'LOCKED' AND u.isDeleted = false")
    List<User> findUsersToUnlock(@Param("now") LocalDateTime now);

    @Query("SELECT u FROM User u WHERE u.lastLogin BETWEEN :start AND :end AND u.isDeleted = false")
    List<User> findByLastLoginBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT u FROM User u WHERE u.lastActive < :threshold AND u.isDeleted = false")
    List<User> findInactiveUsers(@Param("threshold") LocalDateTime threshold);

    // Verification queries
    @Query("SELECT u FROM User u WHERE u.emailVerified = :verified AND u.isDeleted = false")
    List<User> findByEmailVerified(@Param("verified") boolean verified);

    @Query("SELECT u FROM User u WHERE u.phoneVerified = :verified AND u.isDeleted = false")
    List<User> findByPhoneVerified(@Param("verified") boolean verified);

    @Query("SELECT u FROM User u WHERE u.twoFactorEnabled = :enabled AND u.isDeleted = false")
    List<User> findByTwoFactorEnabled(@Param("enabled") boolean enabled);

    // Location-based queries
    @Query("SELECT u FROM User u WHERE u.city = :city AND u.isDeleted = false")
    List<User> findByCity(@Param("city") String city);

    @Query("SELECT u FROM User u WHERE u.country = :country AND u.isDeleted = false")
    List<User> findByCountry(@Param("country") String country);

    @Query("SELECT u FROM User u WHERE u.state = :state AND u.isDeleted = false")
    List<User> findByState(@Param("state") String state);

    // Marketing queries
    @Query("SELECT u FROM User u WHERE u.utmSource = :source AND u.isDeleted = false")
    List<User> findByUtmSource(@Param("source") String source);

    @Query("SELECT u FROM User u WHERE u.referralCode = :code AND u.isDeleted = false")
    Optional<User> findByReferralCode(@Param("code") String code);

    @Query("SELECT u FROM User u WHERE u.referredByUserId = :userId AND u.isDeleted = false")
    List<User> findReferredUsers(@Param("userId") UUID userId);

    // Notification preferences
    @Query("SELECT u FROM User u WHERE u.emailNotifications = true AND u.emailVerified = true AND u.isDeleted = false")
    List<User> findUsersForEmailNotifications();

    @Query("SELECT u FROM User u WHERE u.telegramNotifications = true AND u.telegramUserId IS NOT NULL AND u.isDeleted = false")
    List<User> findUsersForTelegramNotifications();

    @Query("SELECT u FROM User u WHERE u.marketingEmails = true AND u.emailVerified = true AND u.isDeleted = false")
    List<User> findUsersForMarketingEmails();

    // Statistics and counts
    @Query("SELECT COUNT(u) FROM User u WHERE u.accountStatus = 'ACTIVE' AND u.isDeleted = false")
    long countActiveUsers();

    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.name = :role AND u.isDeleted = false")
    long countByRole(@Param("role") RoleEnum role);

    @Query("SELECT COUNT(u) FROM User u WHERE u.userType = :type AND u.isDeleted = false")
    long countByUserType(@Param("type") UserType type);

    @Query("SELECT COUNT(u) FROM User u WHERE u.businessId = :businessId AND u.isDeleted = false")
    long countByBusinessId(@Param("businessId") UUID businessId);

    @Query("SELECT COUNT(u) FROM User u WHERE u.subscriptionPlan = :plan AND u.isDeleted = false")
    long countBySubscriptionPlan(@Param("plan") SubscriptionPlan plan);

    @Query("SELECT COUNT(u) FROM User u WHERE u.customerTier = :tier AND u.isDeleted = false")
    long countByCustomerTier(@Param("tier") CustomerTier tier);

    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt BETWEEN :start AND :end AND u.isDeleted = false")
    long countByCreatedAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Advanced analytics queries
    @Query("SELECT u.country, COUNT(u) FROM User u WHERE u.isDeleted = false GROUP BY u.country")
    List<Object[]> countUsersByCountry();

    @Query("SELECT u.customerTier, COUNT(u) FROM User u WHERE u.isDeleted = false GROUP BY u.customerTier")
    List<Object[]> countUsersByCustomerTier();

    @Query("SELECT u.subscriptionPlan, COUNT(u) FROM User u WHERE u.isDeleted = false GROUP BY u.subscriptionPlan")
    List<Object[]> countUsersBySubscriptionPlan();

    @Query("SELECT DATE(u.createdAt), COUNT(u) FROM User u WHERE u.createdAt BETWEEN :start AND :end AND u.isDeleted = false GROUP BY DATE(u.createdAt)")
    List<Object[]> countUserRegistrationsByDate(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Search queries
    @Query("SELECT u FROM User u WHERE " +
            "(LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.phoneNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.company) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.employeeId) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND u.isDeleted = false")
    Page<User> searchUsers(@Param("search") String search, Pageable pageable);

    // Cleanup queries
    @Query("SELECT u FROM User u WHERE u.accountStatus = 'PENDING_VERIFICATION' AND u.createdAt < :threshold")
    List<User> findUnverifiedUsersOlderThan(@Param("threshold") LocalDateTime threshold);

    @Query("SELECT u FROM User u WHERE u.lastActive < :threshold AND u.accountStatus = 'ACTIVE' AND u.isDeleted = false")
    List<User> findUsersInactiveForDays(@Param("threshold") LocalDateTime threshold);
}