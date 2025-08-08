package com.emenu.features.auth.repository;

import com.emenu.enums.auth.SocialProvider;
import com.emenu.enums.user.RoleEnum;
import com.emenu.enums.user.UserType;
import com.emenu.features.auth.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    // ===== BASIC USER QUERIES =====
    Optional<User> findByUserIdentifierAndIsDeletedFalse(String userIdentifier);
    boolean existsByUserIdentifierAndIsDeletedFalse(String userIdentifier);
    Optional<User> findByIdAndIsDeletedFalse(UUID id);

    // ===== TELEGRAM INTEGRATION QUERIES =====
    Optional<User> findByTelegramUserIdAndIsDeletedFalse(Long telegramUserId);
    boolean existsByTelegramUserIdAndIsDeletedFalse(Long telegramUserId);
    
    @Query("SELECT u FROM User u WHERE u.telegramUserId = :telegramUserId AND u.isDeleted = false")
    Optional<User> findByTelegramUserId(@Param("telegramUserId") Long telegramUserId);
    
    @Query("SELECT u FROM User u WHERE u.telegramUsername = :telegramUsername AND u.isDeleted = false")
    Optional<User> findByTelegramUsername(@Param("telegramUsername") String telegramUsername);

    // ===== NOTIFICATION RECIPIENTS QUERIES =====
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name IN :roles AND u.telegramUserId IS NOT NULL AND u.telegramNotificationsEnabled = true AND u.isDeleted = false")
    List<User> findUsersWithTelegramByRoles(@Param("roles") List<RoleEnum> roles);
    
    @Query("SELECT u FROM User u WHERE u.userType IN :userTypes AND u.telegramUserId IS NOT NULL AND u.telegramNotificationsEnabled = true AND u.isDeleted = false")
    List<User> findUsersWithTelegramByUserTypes(@Param("userTypes") List<UserType> userTypes);
    
    @Query("SELECT u FROM User u WHERE u.telegramUserId IS NOT NULL AND u.telegramNotificationsEnabled = true AND u.isDeleted = false")
    List<User> findAllUsersWithTelegramNotifications();
    
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name IN :roles AND u.isDeleted = false")
    List<User> findUsersByRoles(@Param("roles") List<RoleEnum> roles);

    // ===== BUSINESS CONTEXT QUERIES =====
    @Query("SELECT COUNT(u) FROM User u WHERE u.businessId = :businessId AND u.isDeleted = false")
    long countByBusinessIdAndIsDeletedFalse(@Param("businessId") UUID businessId);
    
    @Query("SELECT u FROM User u WHERE u.businessId = :businessId AND u.telegramUserId IS NOT NULL AND u.telegramNotificationsEnabled = true AND u.isDeleted = false")
    List<User> findBusinessUsersWithTelegram(@Param("businessId") UUID businessId);

    // ===== SOCIAL PROVIDER QUERIES =====
    List<User> findBySocialProviderAndIsDeletedFalse(SocialProvider socialProvider);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.socialProvider = :provider AND u.isDeleted = false")
    long countBySocialProvider(@Param("provider") SocialProvider provider);

    // ===== TELEGRAM ACTIVITY TRACKING =====
    @Modifying
    @Query("UPDATE User u SET u.lastTelegramActivity = :activity WHERE u.telegramUserId = :telegramUserId")
    void updateTelegramActivity(@Param("telegramUserId") Long telegramUserId, @Param("activity") LocalDateTime activity);
    
    @Query("SELECT u FROM User u WHERE u.telegramUserId IS NOT NULL AND u.lastTelegramActivity < :cutoff AND u.isDeleted = false")
    List<User> findInactiveTelegramUsers(@Param("cutoff") LocalDateTime cutoff);

    // ===== ADMIN/PLATFORM QUERIES =====
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name IN ('PLATFORM_OWNER', 'PLATFORM_ADMIN') AND u.isDeleted = false")
    List<User> findPlatformAdmins();
    
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name IN ('BUSINESS_OWNER', 'BUSINESS_MANAGER') AND u.isDeleted = false")
    List<User> findBusinessOwners();
    
    @Query("SELECT u FROM User u WHERE u.userType = 'CUSTOMER' AND u.isDeleted = false")
    List<User> findAllCustomers();

    // ===== STATISTICS QUERIES =====
    @Query("SELECT COUNT(u) FROM User u WHERE u.telegramUserId IS NOT NULL AND u.isDeleted = false")
    long countUsersWithTelegram();
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.socialProvider = 'TELEGRAM' AND u.isDeleted = false")
    long countTelegramOnlyUsers();
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.telegramNotificationsEnabled = true AND u.telegramUserId IS NOT NULL AND u.isDeleted = false")
    long countTelegramNotificationEnabledUsers();

    // ===== COMPLEX QUERIES FOR MULTI-CHANNEL NOTIFICATIONS =====
    @Query("""
        SELECT u FROM User u 
        LEFT JOIN u.roles r 
        WHERE u.isDeleted = false 
        AND (
            (:includePlatformUsers = true AND u.userType = 'PLATFORM_USER') OR
            (:includeBusinessOwners = true AND u.userType = 'BUSINESS_USER') OR
            (:includeCustomers = true AND u.userType = 'CUSTOMER') OR
            (u.id IN :specificUserIds) OR
            (r.name IN :specificRoles)
        )
        """)
    List<User> findNotificationRecipients(
        @Param("includePlatformUsers") boolean includePlatformUsers,
        @Param("includeBusinessOwners") boolean includeBusinessOwners,
        @Param("includeCustomers") boolean includeCustomers,
        @Param("specificUserIds") List<UUID> specificUserIds,
        @Param("specificRoles") List<RoleEnum> specificRoles
    );
}