package com.emenu.features.user_management.service;

import com.emenu.features.user_management.dto.filter.BusinessUserFilterRequest;
import com.emenu.features.user_management.dto.request.CreateBusinessUserRequest;
import com.emenu.features.user_management.dto.response.BusinessUserResponse;
import com.emenu.features.user_management.dto.update.UpdateBusinessUserRequest;
import com.emenu.shared.dto.PaginationResponse;

import java.util.UUID;

public interface BusinessUserService {
    BusinessUserResponse createBusinessUser(CreateBusinessUserRequest request);
    BusinessUserResponse getBusinessUser(UUID id);
    BusinessUserResponse updateBusinessUser(UUID id, UpdateBusinessUserRequest request);
    void deleteBusinessUser(UUID id);
    void changePassword(UUID id, String newPassword);
    PaginationResponse<BusinessUserResponse> listBusinessUsers(BusinessUserFilterRequest filter);
}