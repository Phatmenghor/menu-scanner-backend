package com.emenu.features.location.controller;

import com.emenu.features.location.dto.filter.CommuneFilterRequest;
import com.emenu.features.location.dto.request.CommuneRequest;
import com.emenu.features.location.dto.response.CommuneResponse;
import com.emenu.features.location.service.CommuneService;
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
@RequestMapping("/api/v1/locations/communes")
@RequiredArgsConstructor
@Slf4j
public class CommuneController {

    private final CommuneService communeService;

    /**
     * Retrieves all communes with pagination and filtering
     */
    @PostMapping("/all")
    public ResponseEntity<ApiResponse<PaginationResponse<CommuneResponse>>> getAllCommunes(
            @Valid @RequestBody CommuneFilterRequest request) {
        log.info("Get all communes");
        PaginationResponse<CommuneResponse> response = communeService.getAllCommunes(request);
        return ResponseEntity.ok(ApiResponse.success("Communes retrieved", response));
    }

    /**
     * Retrieves a commune by its ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CommuneResponse>> getCommuneById(@PathVariable UUID id) {
        log.info("Get commune by id: {}", id);
        CommuneResponse response = communeService.getCommuneById(id);
        return ResponseEntity.ok(ApiResponse.success("Commune retrieved", response));
    }

    /**
     * Retrieves a commune by its code
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<CommuneResponse>> getCommuneByCode(@PathVariable String code) {
        log.info("Get commune by code: {}", code);
        CommuneResponse response = communeService.getCommuneByCode(code);
        return ResponseEntity.ok(ApiResponse.success("Commune retrieved", response));
    }

    /**
     * Retrieves a commune by its English name
     */
    @GetMapping("/name-en/{nameEn}")
    public ResponseEntity<ApiResponse<CommuneResponse>> getCommuneByNameEn(@PathVariable String nameEn) {
        log.info("Get commune by EN name: {}", nameEn);
        CommuneResponse response = communeService.getCommuneByNameEn(nameEn);
        return ResponseEntity.ok(ApiResponse.success("Commune retrieved", response));
    }

    /**
     * Retrieves a commune by its Khmer name
     */
    @GetMapping("/name-kh/{nameKh}")
    public ResponseEntity<ApiResponse<CommuneResponse>> getCommuneByNameKh(@PathVariable String nameKh) {
        log.info("Get commune by KH name: {}", nameKh);
        CommuneResponse response = communeService.getCommuneByNameKh(nameKh);
        return ResponseEntity.ok(ApiResponse.success("Commune retrieved", response));
    }

    /**
     * Creates a new commune
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CommuneResponse>> createCommune(
            @Valid @RequestBody CommuneRequest request) {
        log.info("Create commune: {}", request.getCommuneCode());
        CommuneResponse response = communeService.createCommune(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Commune created", response));
    }

    /**
     * Updates an existing commune
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CommuneResponse>> updateCommune(
            @PathVariable UUID id,
            @Valid @RequestBody CommuneRequest request) {
        log.info("Update commune: {}", id);
        CommuneResponse response = communeService.updateCommune(id, request);
        return ResponseEntity.ok(ApiResponse.success("Commune updated", response));
    }

    /**
     * Deletes a commune by its ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCommune(@PathVariable UUID id) {
        log.info("Delete commune: {}", id);
        communeService.deleteCommune(id);
        return ResponseEntity.ok(ApiResponse.success("Commune deleted", null));
    }
}
