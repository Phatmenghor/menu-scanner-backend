package com.emenu.features.location.controller;

import com.emenu.features.location.dto.filter.VillageFilterRequest;
import com.emenu.features.location.dto.request.VillageRequest;
import com.emenu.features.location.dto.response.VillageResponse;
import com.emenu.features.location.service.VillageService;
import com.emenu.shared.dto.ApiResponse;
import com.emenu.shared.dto.PaginationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/locations/villages")
@RequiredArgsConstructor
@Slf4j
public class VillageController {

    private final VillageService villageService;

    @PostMapping("/all")
    public ResponseEntity<ApiResponse<PaginationResponse<VillageResponse>>> getAllVillages(
            @Valid @RequestBody VillageFilterRequest request) {
        log.info("Get all villages");
        PaginationResponse<VillageResponse> response = villageService.getAllVillages(request);
        return ResponseEntity.ok(ApiResponse.success("Villages retrieved", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VillageResponse>> getVillageById(@PathVariable UUID id) {
        log.info("Get village by id: {}", id);
        VillageResponse response = villageService.getVillageById(id);
        return ResponseEntity.ok(ApiResponse.success("Village retrieved", response));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<VillageResponse>> getVillageByCode(@PathVariable String code) {
        log.info("Get village by code: {}", code);
        VillageResponse response = villageService.getVillageByCode(code);
        return ResponseEntity.ok(ApiResponse.success("Village retrieved", response));
    }

    @GetMapping("/name-en/{nameEn}")
    public ResponseEntity<ApiResponse<VillageResponse>> getVillageByNameEn(@PathVariable String nameEn) {
        log.info("Get village by EN name: {}", nameEn);
        VillageResponse response = villageService.getVillageByNameEn(nameEn);
        return ResponseEntity.ok(ApiResponse.success("Village retrieved", response));
    }

    @GetMapping("/name-kh/{nameKh}")
    public ResponseEntity<ApiResponse<VillageResponse>> getVillageByNameKh(@PathVariable String nameKh) {
        log.info("Get village by KH name: {}", nameKh);
        VillageResponse response = villageService.getVillageByNameKh(nameKh);
        return ResponseEntity.ok(ApiResponse.success("Village retrieved", response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<VillageResponse>> createVillage(
            @Valid @RequestBody VillageRequest request) {
        log.info("Create village: {}", request.getVillageCode());
        VillageResponse response = villageService.createVillage(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Village created", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<VillageResponse>> updateVillage(
            @PathVariable UUID id,
            @Valid @RequestBody VillageRequest request) {
        log.info("Update village: {}", id);
        VillageResponse response = villageService.updateVillage(id, request);
        return ResponseEntity.ok(ApiResponse.success("Village updated", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteVillage(@PathVariable UUID id) {
        log.info("Delete village: {}", id);
        villageService.deleteVillage(id);
        return ResponseEntity.ok(ApiResponse.success("Village deleted", null));
    }
}