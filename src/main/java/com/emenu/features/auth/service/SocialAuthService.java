package com.emenu.features.auth.service;

import com.emenu.features.auth.dto.request.SocialAuthRequest;
import com.emenu.features.auth.dto.response.SocialAuthResponse;
import com.emenu.features.auth.dto.response.SocialSyncResponse;

/**
 * Main service for social authentication operations
 * Coordinates different provider services dynamically
 */
public interface SocialAuthService {

    /**
     * Authenticate user with social provider (login or register)
     * @param request Social auth request
     * @return Authentication response with tokens
     */
    SocialAuthResponse authenticate(SocialAuthRequest request);

    /**
     * Sync social account to currently logged-in user
     * @param request Social auth request
     * @return Sync response
     */
    SocialSyncResponse syncSocialAccount(SocialAuthRequest request);

    /**
     * Unsync social account from currently logged-in user
     * @param provider Provider to unsync
     * @return Sync response
     */
    SocialSyncResponse unsyncSocialAccount(String provider);
}
