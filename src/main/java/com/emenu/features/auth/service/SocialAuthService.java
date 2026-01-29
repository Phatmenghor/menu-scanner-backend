package com.emenu.features.auth.service;

import com.emenu.features.auth.dto.request.SocialAuthRequest;
import com.emenu.features.auth.dto.response.SocialAuthResponse;
import com.emenu.features.auth.dto.response.SocialSyncResponse;

public interface SocialAuthService {

    SocialAuthResponse authenticate(SocialAuthRequest request);

    SocialSyncResponse syncSocialAccount(SocialAuthRequest request);

    SocialSyncResponse unsyncSocialAccount(String provider);
}
