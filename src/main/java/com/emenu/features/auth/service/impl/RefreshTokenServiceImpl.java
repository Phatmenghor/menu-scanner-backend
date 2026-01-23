package com.emenu.features.auth.service.impl;

import com.emenu.features.auth.models.RefreshToken;
import com.emenu.features.auth.models.User;
import com.emenu.features.auth.repository.RefreshTokenRepository;
import com.emenu.features.auth.service.RefreshTokenService;
import com.emenu.security.jwt.JWTGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of RefreshTokenService for managing refresh tokens.
 *
 * @author Cambodia E-Menu Platform
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JWTGenerator jwtGenerator;

    @Override
    @Transactional
    public RefreshToken createRefreshToken(User user, String ipAddress, String deviceInfo) {
        log.info("Creating refresh token for user: {}", user.getUserIdentifier());

        // Generate JWT refresh token
        String tokenString = jwtGenerator.generateRefreshToken(user.getUserIdentifier());

        // Create refresh token entity
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(tokenString);
        refreshToken.setUserId(user.getId());
        refreshToken.setExpiryDate(
                LocalDateTime.ofInstant(
                        jwtGenerator.getRefreshTokenExpiryDate().toInstant(),
                        ZoneId.systemDefault()
                )
        );
        refreshToken.setIsRevoked(false);
        refreshToken.setIpAddress(ipAddress);
        refreshToken.setDeviceInfo(deviceInfo);

        RefreshToken saved = refreshTokenRepository.save(refreshToken);
        log.info("Refresh token created successfully for user: {}", user.getUserIdentifier());

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RefreshToken> verifyRefreshToken(String token) {
        log.debug("Verifying refresh token");

        // Validate JWT structure
        if (!jwtGenerator.validateToken(token)) {
            log.warn("Invalid refresh token JWT structure");
            return Optional.empty();
        }

        // Check if token is expired in JWT
        if (jwtGenerator.isTokenExpired(token)) {
            log.warn("Refresh token is expired (JWT check)");
            return Optional.empty();
        }

        // Find token in database
        Optional<RefreshToken> refreshTokenOpt = refreshTokenRepository.findByTokenAndIsValidTrue(token);

        if (refreshTokenOpt.isEmpty()) {
            log.warn("Refresh token not found or invalid in database");
            return Optional.empty();
        }

        RefreshToken refreshToken = refreshTokenOpt.get();

        // Additional validation
        if (!refreshToken.isValid()) {
            log.warn("Refresh token is not valid: expired={}, revoked={}, deleted={}",
                    refreshToken.isExpired(), refreshToken.getIsRevoked(), refreshToken.getIsDeleted());
            return Optional.empty();
        }

        log.debug("Refresh token verified successfully");
        return Optional.of(refreshToken);
    }

    @Override
    @Transactional
    public void revokeRefreshToken(String token, String reason) {
        log.info("Revoking refresh token with reason: {}", reason);

        Optional<RefreshToken> refreshTokenOpt = refreshTokenRepository.findByToken(token);

        if (refreshTokenOpt.isPresent()) {
            RefreshToken refreshToken = refreshTokenOpt.get();
            refreshToken.revoke(reason);
            refreshTokenRepository.save(refreshToken);
            log.info("Refresh token revoked successfully");
        } else {
            log.warn("Refresh token not found for revocation");
        }
    }

    @Override
    @Transactional
    public void revokeAllUserTokens(UUID userId, String reason) {
        log.info("Revoking all refresh tokens for user: {} with reason: {}", userId, reason);

        int revokedCount = refreshTokenRepository.revokeAllByUserId(
                userId,
                LocalDateTime.now(),
                reason
        );

        log.info("Revoked {} refresh tokens for user: {}", revokedCount, userId);
    }

    @Override
    @Transactional
    public void deleteExpiredTokens() {
        log.info("Starting cleanup of expired and revoked refresh tokens");

        LocalDateTime now = LocalDateTime.now();
        // Delete tokens revoked more than 7 days ago
        LocalDateTime cutoffDate = now.minusDays(7);

        int deletedCount = refreshTokenRepository.deleteExpiredAndRevokedTokens(now, cutoffDate);

        log.info("Deleted {} expired/revoked refresh tokens", deletedCount);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }
}
