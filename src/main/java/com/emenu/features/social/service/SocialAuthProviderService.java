package com.emenu.features.social.service;

import com.emenu.enums.social.SocialAuthProvider;
import com.emenu.features.auth.models.User;

import java.util.Map;

/**
 * Interface for social authentication provider services
 * Each provider (Telegram, Google, Facebook, etc.) implements this interface
 */
public interface SocialAuthProviderService {

    /**
     * Get the provider type this service handles
     */
    SocialAuthProvider getProvider();

    /**
     * Validate authentication data from the provider
     * @param authData Provider-specific authentication data
     * @return Validated social user data
     * @throws com.emenu.exception.custom.ValidationException if validation fails
     */
    SocialUserData validateAuth(Map<String, Object> authData);

    /**
     * Find user by social provider ID
     * @param socialId Provider-specific user ID
     * @return User if found, null otherwise
     */
    User findUserBySocialId(String socialId);

    /**
     * Sync social account to user
     * @param user User to sync to
     * @param socialUserData Validated social user data
     */
    void syncToUser(User user, SocialUserData socialUserData);

    /**
     * Unsync social account from user
     * @param user User to unsync from
     */
    void unsyncFromUser(User user);

    /**
     * Check if user has this provider synced
     * @param user User to check
     * @return true if synced, false otherwise
     */
    boolean isSynced(User user);

    /**
     * Data class for validated social user information
     */
    class SocialUserData {
        public String socialId;
        public String username;
        public String email;
        public String firstName;
        public String lastName;
        public Map<String, Object> additionalData;

        public SocialUserData(String socialId, String username, String email, String firstName, String lastName) {
            this.socialId = socialId;
            this.username = username;
            this.email = email;
            this.firstName = firstName;
            this.lastName = lastName;
        }
    }
}
