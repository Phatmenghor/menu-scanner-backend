package com.emenu.features.location.controller;

import com.emenu.features.location.dto.filter.DistrictFilterRequest;
import com.emenu.features.location.dto.request.DistrictRequest;
import com.emenu.features.location.dto.response.DistrictResponse;
import com.emenu.features.location.service.DistrictService;
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
@RequestMapping("/api/v1/locations/districts")
@RequiredArgsConstructor
@Slf4j
public class DistrictController {

    private final DistrictService districtService;

    @PostMapping("/all")
    public ResponseEntity<ApiResponse<PaginationResponse<DistrictResponse>>> getAllDistricts(
            @Valid @RequestBody DistrictFilterRequest request) {
        log.info("Get all districts");
        PaginationResponse<DistrictResponse> response = districtService.getAllDistricts(request);
        return ResponseEntity.ok(ApiResponse.success("Districts retrieved", response));
    }

    @GetMapping("/by-province/{provinceCode}")
    public ResponseEntity<ApiResponse<List<DistrictResponse>>> getDistrictsByProvinceCode(
            @PathVariable String provinceCode) {
        log.info("Get districts by province code: {}", provinceCode);
        List<DistrictResponse> response = districtService.getDistrictsByProvinceCode(provinceCode);
        return ResponseEntity.ok(ApiResponse.success("Districts retrieved", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DistrictResponse>> getDistrictById(@PathVariable UUID id) {
        log.info("Get district by id: {}", id);
        DistrictResponse response = districtService.getDistrictById(id);
        return ResponseEntity.ok(ApiResponse.success("District retrieved", response));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<DistrictResponse>> getDistrictByCode(@PathVariable String code) {
        log.info("Get district by code: {}", code);
        DistrictResponse response = districtService.getDistrictByCode(code);
        return ResponseEntity.ok(ApiResponse.success("District retrieved", response));
    }

    @GetMapping("/name-en/{nameEn}")
    public ResponseEntity<ApiResponse<DistrictResponse>> getDistrictByNameEn(@PathVariable String nameEn) {
        log.info("Get district by EN name: {}", nameEn);
        DistrictResponse response = districtService.getDistrictByNameEn(nameEn);
        return ResponseEntity.ok(ApiResponse.success("District retrieved", response));
    }

    @GetMapping("/name-kh/{nameKh}")
    public ResponseEntity<ApiResponse<DistrictResponse>> getDistrictByNameKh(@PathVariable String nameKh) {
        log.info("Get district by KH name: {}", nameKh);
        DistrictResponse response = districtService.getDistrictByNameKh(nameKh);
        return ResponseEntity.ok(ApiResponse.success("District retrieved", response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DistrictResponse>> createDistrict(
            @Valid @RequestBody DistrictRequest request) {
        log.info("Create district: {}", request.getDistrictCode());
        DistrictResponse response = districtService.createDistrict(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("District created", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DistrictResponse>> updateDistrict(
            @PathVariable UUID id,
            @Valid @RequestBody DistrictRequest request) {
        log.info("Update district: {}", id);
        DistrictResponse response = districtService.updateDistrict(id, request);
        return ResponseEntity.ok(ApiResponse.success("District updated", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDistrict(@PathVariable UUID id) {
        log.info("Delete district: {}", id);
        districtService.deleteDistrict(id);
        return ResponseEntity.ok(ApiResponse.success("District deleted", null));
    }
}