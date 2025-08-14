package com.emenu.features.business.controller;

import com.emenu.features.business.dto.filter.BannerFilterRequest;
import com.emenu.features.business.dto.response.BannerResponse;
import com.emenu.features.business.service.BannerService;
import com.emenu.security.SecurityUtils;
import com.emenu.shared.dto.ApiResponse;
import com.emenu.shared.dto.PaginationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/public/banners")
@RequiredArgsConstructor
@Slf4j
public class PublicBannerController {
    private final BannerService bannerService;
    private final SecurityUtils securityUtils;

    /**
     * Get all banners with filtering
     */
    @PostMapping("/my-business/all")
    public ResponseEntity<ApiResponse<List<BannerResponse>>> getMyBusinessAllBanners(@Valid @RequestBody BannerFilterRequest filter) {
        log.info("Getting my banners for current user's business");
        UUID getCurrentUserId = securityUtils.getCurrentUserId();
        filter.setBusinessId(getCurrentUserId);
        List<BannerResponse> banners = bannerService.getAllItemBanners(filter);
        return ResponseEntity.ok(ApiResponse.success("Banners retrieved successfully", banners));
    }
}
