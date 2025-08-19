package com.emenu.features.business.controller;

import com.emenu.features.business.dto.filter.BannerAllFilterRequest;
import com.emenu.features.business.dto.response.BannerResponse;
import com.emenu.features.business.service.BannerService;
import com.emenu.security.SecurityUtils;
import com.emenu.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public/banners")
@RequiredArgsConstructor
@Slf4j
public class PublicBannerController {
    private final BannerService bannerService;

    /**
     * Get all banners with filtering
     */
    @PostMapping("/all")
    public ResponseEntity<ApiResponse<List<BannerResponse>>> getMyBusinessAllBanners(@Valid @RequestBody BannerAllFilterRequest filter) {
        log.info("Getting my banners for current user's business");
        List<BannerResponse> banners = bannerService.getAllItemBanners(filter);
        return ResponseEntity.ok(ApiResponse.success("Banners retrieved successfully", banners));
    }
}
