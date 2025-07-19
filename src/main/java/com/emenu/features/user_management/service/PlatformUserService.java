package com.emenu.features.user_management.service;

import com.emenu.features.user_management.dto.filter.PlatformUserFilterRequest;
import com.emenu.features.user_management.dto.request.CreatePlatformUserRequest;
import com.emenu.features.user_management.dto.response.PlatformUserResponse;
import com.emenu.features.user_management.dto.update.UpdatePlatformUserRequest;
import com.emenu.shared.dto.PaginationResponse;

import java.util.UUID;

public interface PlatformUserService {
    PlatformUserResponse createPlatformUser(CreatePlatformUserRequest request);
    PlatformUserResponse getPlatformUser(UUID id);
    PlatformUserResponse updatePlatformUser(UUID id, UpdatePlatformUserRequest request);
    void deletePlatformUser(UUID id);
    PaginationResponse<PlatformUserResponse> listPlatformUsers(PlatformUserFilterRequest filter);
}