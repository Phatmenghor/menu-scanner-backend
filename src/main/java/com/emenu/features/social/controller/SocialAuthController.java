package com.emenu.features.social.controller;

import com.emenu.features.social.dto.request.SocialAuthRequest;
import com.emenu.features.social.dto.response.SocialAuthResponse;
import com.emenu.features.social.dto.response.SocialSyncResponse;
import com.emenu.features.social.service.SocialAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth/social")
@RequiredArgsConstructor
public class SocialAuthController {

    private final SocialAuthService socialAuthService;

    @PostMapping("/authenticate")
    public ResponseEntity<SocialAuthResponse> authenticate(
            @Valid @RequestBody SocialAuthRequest request,
            HttpServletRequest httpRequest) {
        
        request.setIpAddress(getClientIp(httpRequest));
        request.setDeviceInfo(getUserAgent(httpRequest));

        SocialAuthResponse response = socialAuthService.authenticate(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/sync")
    public ResponseEntity<SocialSyncResponse> syncAccount(
            @Valid @RequestBody SocialAuthRequest request) {
        
        SocialSyncResponse response = socialAuthService.syncSocialAccount(request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/sync/{provider}")
    public ResponseEntity<SocialSyncResponse> unsyncAccount(@PathVariable String provider) {
        SocialSyncResponse response = socialAuthService.unsyncSocialAccount(provider);
        return ResponseEntity.ok(response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String getUserAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }
}
