package com.emenu.features.auth.service.social.provider;

public record SocialUserInfo(
        String id,
        String username,
        String email,
        String firstName,
        String lastName
) {}
