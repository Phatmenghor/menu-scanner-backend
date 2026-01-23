package com.emenu.features.auth.service;

import com.emenu.features.auth.models.RefreshToken;
import com.emenu.features.auth.models.User;

import java.util.Optional;
import java.util.UUID;

/**
 * Service interface for managing refresh tokens.
 *
 * @author Cambodia E-Menu Platform
 * @version 1.0.0
 */
public interface RefreshTokenService {

    /**
     * Create a new refresh token for a user
     *
     * @param user the user
     * @param ipAddress IP address of the request
     * @param deviceInfo device/client information
     * @return the created refresh token entity
     */
    RefreshToken createRefreshToken(User user, String ipAddress, String deviceInfo);

    /**
     * Verify and retrieve a refresh token
     *
     * @param token the token string
     * @return Optional containing the token if valid
     */
    Optional<RefreshToken> verifyRefreshToken(String token);

    /**
     * Revoke a specific refresh token
     *
     * @param token the token string
     * @param reason the reason for revocation
     */
    void revokeRefreshToken(String token, String reason);

    /**
     * Revoke all refresh tokens for a user
     *
     * @param userId the user ID
     * @param reason the reason for revocation
     */
    void revokeAllUserTokens(UUID userId, String reason);

    /**
     * Delete expired and revoked tokens (cleanup job)
     */
    void deleteExpiredTokens();

    /**
     * Get a refresh token by token string
     *
     * @param token the token string
     * @return Optional containing the token if found
     */
    Optional<RefreshToken> findByToken(String token);
}
