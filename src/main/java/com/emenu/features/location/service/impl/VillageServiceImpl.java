package com.emenu.features.location.service.impl;

import com.emenu.exception.custom.ValidationException;
import com.emenu.features.location.dto.filter.VillageFilterRequest;
import com.emenu.features.location.dto.request.VillageRequest;
import com.emenu.features.location.dto.response.VillageResponse;
import com.emenu.features.location.mapper.VillageMapper;
import com.emenu.features.location.models.Village;
import com.emenu.features.location.repository.CommuneRepository;
import com.emenu.features.location.repository.VillageRepository;
import com.emenu.features.location.service.VillageService;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.pagination.PaginationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VillageServiceImpl implements VillageService {

    private final VillageRepository villageRepository;
    private final VillageMapper villageMapper;
    private final CommuneRepository communeRepository;

    @Override
    @Transactional
    public VillageResponse createVillage(VillageRequest request) {
        log.info("Creating village: {}", request.getVillageCode());
        
        if (!communeRepository.existsByCommuneCodeAndIsDeletedFalse(request.getCommuneCode())) {
            throw new ValidationException("Commune code does not exist: " + request.getCommuneCode());
        }
        
        if (villageRepository.existsByVillageCodeAndIsDeletedFalse(request.getVillageCode())) {
            throw new ValidationException("Village code already exists");
        }
        
        Village village = villageMapper.toEntity(request);
        Village savedVillage = villageRepository.save(village);
        
        // Fetch with full hierarchy loaded
        Village villageWithRelations = villageRepository
            .findByIdAndIsDeletedFalse(savedVillage.getId())
            .orElseThrow(() -> new RuntimeException("Village not found"));
        
        // Map to response WITHIN transaction
        VillageResponse response = villageMapper.toResponse(villageWithRelations);
        
        log.info("Village created: {} with full hierarchy loaded", 
                 villageWithRelations.getVillageCode());
        
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<VillageResponse> getAllVillages(VillageFilterRequest request) {
        log.info("Getting all villages with filters");
        
        Pageable pageable = PaginationUtils.createPageable(
            request.getPageNo(), request.getPageSize(),
            request.getSortBy(), request.getSortDirection()
        );
        
        Page<Village> villagePage = villageRepository.searchVillages(
            request.getCommuneCode(), request.getDistrictCode(), 
            request.getProvinceCode(), request.getSearch(), pageable
        );
        
        // Map WITHIN transaction
        return villageMapper.toPaginationResponse(villagePage);
    }

    @Override
    @Transactional(readOnly = true)
    public VillageResponse getVillageById(UUID id) {
        Village village = villageRepository.findByIdAndIsDeletedFalse(id)
            .orElseThrow(() -> new RuntimeException("Village not found"));
        return villageMapper.toResponse(village);
    }

    @Override
    @Transactional(readOnly = true)
    public VillageResponse getVillageByCode(String code) {
        Village village = villageRepository.findByVillageCodeAndIsDeletedFalse(code)
            .orElseThrow(() -> new RuntimeException("Village not found with code: " + code));
        return villageMapper.toResponse(village);
    }

    @Override
    @Transactional(readOnly = true)
    public VillageResponse getVillageByNameEn(String nameEn) {
        Village village = villageRepository.findByVillageEnAndIsDeletedFalse(nameEn)
            .orElseThrow(() -> new RuntimeException("Village not found with name: " + nameEn));
        return villageMapper.toResponse(village);
    }

    @Override
    @Transactional(readOnly = true)
    public VillageResponse getVillageByNameKh(String nameKh) {
        Village village = villageRepository.findByVillageKhAndIsDeletedFalse(nameKh)
            .orElseThrow(() -> new RuntimeException("Village not found with Khmer name: " + nameKh));
        return villageMapper.toResponse(village);
    }

    @Override
    @Transactional
    public VillageResponse updateVillage(UUID id, VillageRequest request) {
        log.info("Updating village: {}", id);
        
        Village village = villageRepository.findByIdAndIsDeletedFalse(id)
            .orElseThrow(() -> new RuntimeException("Village not found"));
        
        if (request.getCommuneCode() != null && 
            !request.getCommuneCode().equals(village.getCommuneCode())) {
            if (!communeRepository.existsByCommuneCodeAndIsDeletedFalse(request.getCommuneCode())) {
                throw new ValidationException("Commune code does not exist: " + request.getCommuneCode());
            }
        }
        
        villageMapper.updateEntity(request, village);
        villageRepository.save(village);
        
        // Fetch updated village with full hierarchy
        Village updatedVillage = villageRepository.findByIdAndIsDeletedFalse(id)
            .orElseThrow(() -> new RuntimeException("Village not found"));
        
        log.info("Village updated: {}", updatedVillage.getVillageCode());
        return villageMapper.toResponse(updatedVillage);
    }

    @Override
    @Transactional
    public void deleteVillage(UUID id) {
        Village village = villageRepository.findByIdAndIsDeletedFalse(id)
            .orElseThrow(() -> new RuntimeException("Village not found"));
        
        village.softDelete();
        villageRepository.save(village);
        log.info("Village deleted: {}", village.getVillageCode());
    }
}