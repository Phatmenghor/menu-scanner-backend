package com.emenu.features.location.service.impl;

import com.emenu.exception.custom.ValidationException;
import com.emenu.features.location.dto.filter.CommuneFilterRequest;
import com.emenu.features.location.dto.request.CommuneRequest;
import com.emenu.features.location.dto.response.CommuneResponse;
import com.emenu.features.location.mapper.CommuneMapper;
import com.emenu.features.location.models.Commune;
import com.emenu.features.location.repository.CommuneRepository;
import com.emenu.features.location.service.CommuneService;
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
public class CommuneServiceImpl implements CommuneService {

    private final CommuneRepository communeRepository;
    private final CommuneMapper communeMapper;

    @Override
    public CommuneResponse createCommune(CommuneRequest request) {
        log.info("Creating commune: {}", request.getCommuneCode());
        
        if (communeRepository.existsByCommuneCodeAndIsDeletedFalse(request.getCommuneCode())) {
            throw new ValidationException("Commune code already exists");
        }
        
        Commune commune = communeMapper.toEntity(request);
        Commune savedCommune = communeRepository.save(commune);
        Commune communeWithRelations = communeRepository.findByIdAndIsDeletedFalse(savedCommune.getId())
            .orElseThrow(() -> new RuntimeException("Commune not found"));
        
        log.info("Commune created: {}", communeWithRelations.getCommuneCode());
        return communeMapper.toResponse(communeWithRelations);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<CommuneResponse> getAllCommunes(CommuneFilterRequest request) {
        log.info("Getting all communes with filters");
        
        Pageable pageable = PaginationUtils.createPageable(
            request.getPageNo(), request.getPageSize(),
            request.getSortBy(), request.getSortDirection()
        );
        
        Page<Commune> communePage = communeRepository.searchCommunes(
            request.getDistrictCode(), request.getSearch(), pageable
        );
        
        return communeMapper.toPaginationResponse(communePage);
    }

    @Override
    @Transactional(readOnly = true)
    public CommuneResponse getCommuneById(UUID id) {
        Commune commune = communeRepository.findByIdAndIsDeletedFalse(id)
            .orElseThrow(() -> new RuntimeException("Commune not found"));
        return communeMapper.toResponse(commune);
    }

    @Override
    @Transactional(readOnly = true)
    public CommuneResponse getCommuneByCode(String code) {
        Commune commune = communeRepository.findByCommuneCodeAndIsDeletedFalse(code)
            .orElseThrow(() -> new RuntimeException("Commune not found with code: " + code));
        return communeMapper.toResponse(commune);
    }

    @Override
    @Transactional(readOnly = true)
    public CommuneResponse getCommuneByNameEn(String nameEn) {
        Commune commune = communeRepository.findByCommuneEnAndIsDeletedFalse(nameEn)
            .orElseThrow(() -> new RuntimeException("Commune not found with name: " + nameEn));
        return communeMapper.toResponse(commune);
    }

    @Override
    @Transactional(readOnly = true)
    public CommuneResponse getCommuneByNameKh(String nameKh) {
        Commune commune = communeRepository.findByCommuneKhAndIsDeletedFalse(nameKh)
            .orElseThrow(() -> new RuntimeException("Commune not found with Khmer name: " + nameKh));
        return communeMapper.toResponse(commune);
    }

    @Override
    public CommuneResponse updateCommune(UUID id, CommuneRequest request) {
        log.info("Updating commune: {}", id);
        
        Commune commune = communeRepository.findByIdAndIsDeletedFalse(id)
            .orElseThrow(() -> new RuntimeException("Commune not found"));
        
        communeMapper.updateEntity(request, commune);
        communeRepository.save(commune);
        
        Commune updatedCommune = communeRepository.findByIdAndIsDeletedFalse(id)
            .orElseThrow(() -> new RuntimeException("Commune not found"));
        
        log.info("Commune updated: {}", updatedCommune.getCommuneCode());
        return communeMapper.toResponse(updatedCommune);
    }

    @Override
    public void deleteCommune(UUID id) {
        Commune commune = communeRepository.findByIdAndIsDeletedFalse(id)
            .orElseThrow(() -> new RuntimeException("Commune not found"));
        
        commune.softDelete();
        communeRepository.save(commune);
        log.info("Commune deleted: {}", commune.getCommuneCode());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommuneResponse> getCommunesByDistrictCode(String districtCode) {
        List<Commune> communes = communeRepository.findAllByDistrictCodeAndIsDeletedFalse(districtCode);
        return communeMapper.toResponseList(communes);
    }
}
