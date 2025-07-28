package com.emenu.features.auth.repository;

import com.emenu.enums.auth.SocialProvider;
import com.emenu.features.auth.models.SocialUserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SocialUserAccountRepository extends JpaRepository<SocialUserAccount, UUID> {

    /**
     * Find social account by provider and provider ID
     */
    Optional<SocialUserAccount> findByProviderAndProviderIdAndIsDeletedFalse(
            SocialProvider provider, String providerId);

    /**
     * Find social account by provider and username (for Telegram)
     */
    Optional<SocialUserAccount> findByProviderAndProviderUsernameAndIsDeletedFalse(
            SocialProvider provider, String providerUsername);

    /**
     * Find all social accounts for a user
     */
    List<SocialUserAccount> findByUserIdAndIsDeletedFalse(UUID userId);

    /**
     * Find user with social account details
     */
    @Query("SELECT s FROM SocialUserAccount s " +
            "JOIN FETCH s.user u " +
            "WHERE s.provider = :provider " +
            "AND s.providerId = :providerId " +
            "AND s.isDeleted = false " +
            "AND u.isDeleted = false")
    Optional<SocialUserAccount> findWithUserByProviderAndProviderId(
            @Param("provider") SocialProvider provider,
            @Param("providerId") String providerId);


    /**
     * Count social accounts for a specific user
     */
    long countByUserIdAndIsDeletedFalse(UUID userId);

    /**
     * Check if user has any social accounts
     */
    boolean existsByUserIdAndIsDeletedFalse(UUID userId);
}