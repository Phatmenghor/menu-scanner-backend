package com.emenu.features.location.service.impl;

import com.emenu.exception.custom.ValidationException;
import com.emenu.features.location.dto.filter.VillageFilterRequest;
import com.emenu.features.location.dto.request.VillageRequest;
import com.emenu.features.location.dto.response.VillageResponse;
import com.emenu.features.location.mapper.VillageMapper;
import com.emenu.features.location.models.Village;
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
@Transactional
public class VillageServiceImpl implements VillageService {

    private final VillageRepository villageRepository;
    private final VillageMapper villageMapper;

    @Override
    public VillageResponse createVillage(VillageRequest request) {
        log.info("Creating village: {}", request.getVillageCode());
        
        if (villageRepository.existsByVillageCodeAndIsDeletedFalse(request.getVillageCode())) {
            throw new ValidationException("Village code already exists");
        }
        
        Village village = villageMapper.toEntity(request);
        Village savedVillage = villageRepository.save(village);
        Village villageWithRelations = villageRepository.findByIdAndIsDeletedFalse(savedVillage.getId())
            .orElseThrow(() -> new RuntimeException("Village not found"));
        
        log.info("Village created: {}", villageWithRelations.getVillageCode());
        return villageMapper.toResponse(villageWithRelations);
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
            request.getCommuneCode(), request.getSearch(), pageable
        );
        
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
    public VillageResponse updateVillage(UUID id, VillageRequest request) {
        log.info("Updating village: {}", id);
        
        Village village = villageRepository.findByIdAndIsDeletedFalse(id)
            .orElseThrow(() -> new RuntimeException("Village not found"));
        
        villageMapper.updateEntity(request, village);
        villageRepository.save(village);
        
        Village updatedVillage = villageRepository.findByIdAndIsDeletedFalse(id)
            .orElseThrow(() -> new RuntimeException("Village not found"));
        
        log.info("Village updated: {}", updatedVillage.getVillageCode());
        return villageMapper.toResponse(updatedVillage);
    }

    @Override
    public void deleteVillage(UUID id) {
        Village village = villageRepository.findByIdAndIsDeletedFalse(id)
            .orElseThrow(() -> new RuntimeException("Village not found"));
        
        village.softDelete();
        villageRepository.save(village);
        log.info("Village deleted: {}", village.getVillageCode());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VillageResponse> getVillagesByCommuneCode(String communeCode) {
        List<Village> villages = villageRepository.findAllByCommuneCodeAndIsDeletedFalse(communeCode);
        return villageMapper.toResponseList(villages);
    }
}