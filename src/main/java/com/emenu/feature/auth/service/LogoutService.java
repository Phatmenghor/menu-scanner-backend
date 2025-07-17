package com.emenu.feature.auth.service;

public interface LogoutService {
    /**
     * Logout and blacklist the current token
     * @param token JWT token to be invalidated
     */
    void logout(String token);
}