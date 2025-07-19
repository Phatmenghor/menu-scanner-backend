package com.emenu.features.user_management.controller;

import com.emenu.features.user_management.dto.filter.PlatformUserFilterRequest;
import com.emenu.features.user_management.dto.request.CreatePlatformUserRequest;
import com.emenu.features.user_management.dto.response.PlatformUserResponse;
import com.emenu.features.user_management.dto.update.UpdatePlatformUserRequest;
import com.emenu.shared.dto.ApiResponse;
import com.emenu.shared.dto.PaginationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/platform-users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Platform Users", description = "Platform user management")
@PreAuthorize("hasRole('PLATFORM_OWNER') or hasRole('PLATFORM_ADMIN')")
public class PlatformUserController {

    private final PlatformUserService platformUserService;

    @PostMapping
    @Operation(summary = "Create platform user")
    public ResponseEntity<ApiResponse<PlatformUserResponse>> createPlatformUser(
            @Valid @RequestBody CreatePlatformUserRequest request) {
        log.info("Creating platform user: {}", request.getEmail());
        PlatformUserResponse response = platformUserService.createPlatformUser(request);
        return ResponseEntity.ok(ApiResponse.success("Platform user created successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get platform user by ID")
    public ResponseEntity<ApiResponse<PlatformUserResponse>> getPlatformUser(@PathVariable UUID id) {
        log.info("Getting platform user: {}", id);
        PlatformUserResponse response = platformUserService.getPlatformUser(id);
        return ResponseEntity.ok(ApiResponse.success("Platform user retrieved successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update platform user")
    public ResponseEntity<ApiResponse<PlatformUserResponse>> updatePlatformUser(
            @PathVariable UUID id, @Valid @RequestBody UpdatePlatformUserRequest request) {
        log.info("Updating platform user: {}", id);
        PlatformUserResponse response = platformUserService.updatePlatformUser(id, request);
        return ResponseEntity.ok(ApiResponse.success("Platform user updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete platform user")
    public ResponseEntity<ApiResponse<Void>> deletePlatformUser(@PathVariable UUID id) {
        log.info("Deleting platform user: {}", id);
        platformUserService.deletePlatformUser(id);
        return ResponseEntity.ok(ApiResponse.success("Platform user deleted successfully", null));
    }

    @GetMapping
    @Operation(summary = "List platform users")
    public ResponseEntity<ApiResponse<PaginationResponse<PlatformUserResponse>>> listPlatformUsers(
            @ModelAttribute PlatformUserFilterRequest filter) {
        log.info("Listing platform users");
        PaginationResponse<PlatformUserResponse> response = platformUserService.listPlatformUsers(filter);
        return ResponseEntity.ok(ApiResponse.success("Platform users retrieved successfully", response));
    }
}
