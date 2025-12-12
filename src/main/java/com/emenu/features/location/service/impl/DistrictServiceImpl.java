package com.emenu.features.location.service.impl;

import com.emenu.exception.custom.ValidationException;
import com.emenu.features.location.dto.filter.DistrictFilterRequest;
import com.emenu.features.location.dto.request.DistrictRequest;
import com.emenu.features.location.dto.response.DistrictResponse;
import com.emenu.features.location.mapper.DistrictMapper;
import com.emenu.features.location.models.District;
import com.emenu.features.location.repository.DistrictRepository;
import com.emenu.features.location.repository.ProvinceRepository;
import com.emenu.features.location.service.DistrictService;
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
public class DistrictServiceImpl implements DistrictService {

    private final DistrictRepository districtRepository;
    private final DistrictMapper districtMapper;
    private final ProvinceRepository provinceRepository;

    @Override
    @Transactional  // Keep transaction open during mapping
    public DistrictResponse createDistrict(DistrictRequest request) {
        log.info("Creating district: {}", request.getDistrictCode());
        
        if (!provinceRepository.existsByProvinceCodeAndIsDeletedFalse(request.getProvinceCode())) {
            throw new ValidationException("Province code does not exist: " + request.getProvinceCode());
        }
        
        if (districtRepository.existsByDistrictCodeAndIsDeletedFalse(request.getDistrictCode())) {
            throw new ValidationException("District code already exists");
        }
        
        District district = districtMapper.toEntity(request);
        District savedDistrict = districtRepository.save(district);
        
        // Fetch with province loaded
        District districtWithProvince = districtRepository
            .findByIdAndIsDeletedFalse(savedDistrict.getId())
            .orElseThrow(() -> new RuntimeException("District not found"));
        
        // Map to response WITHIN transaction
        DistrictResponse response = districtMapper.toResponse(districtWithProvince);
        
        log.info("District created: {} with province: {}", 
                 districtWithProvince.getDistrictCode(),
                 districtWithProvince.getProvince() != null ? 
                 districtWithProvince.getProvince().getProvinceCode() : "null");
        
        return response;
    }

    @Override
    @Transactional(readOnly = true)  // Keep transaction open during mapping
    public PaginationResponse<DistrictResponse> getAllDistricts(DistrictFilterRequest request) {
        log.info("Getting all districts with filters");
        
        Pageable pageable = PaginationUtils.createPageable(
            request.getPageNo(), request.getPageSize(),
            request.getSortBy(), request.getSortDirection()
        );
        
        Page<District> districtPage = districtRepository.searchDistricts(
            request.getProvinceCode(), request.getSearch(), pageable
        );
        
        // Map WITHIN transaction
        return districtMapper.toPaginationResponse(districtPage);
    }

    @Override
    @Transactional(readOnly = true)  // Keep transaction open
    public DistrictResponse getDistrictById(UUID id) {
        District district = districtRepository.findByIdAndIsDeletedFalse(id)
            .orElseThrow(() -> new RuntimeException("District not found"));
        
        // Map WITHIN transaction
        return districtMapper.toResponse(district);
    }

    @Override
    @Transactional(readOnly = true)
    public DistrictResponse getDistrictByCode(String code) {
        District district = districtRepository.findByDistrictCodeAndIsDeletedFalse(code)
            .orElseThrow(() -> new RuntimeException("District not found with code: " + code));
        return districtMapper.toResponse(district);
    }

    @Override
    @Transactional(readOnly = true)
    public DistrictResponse getDistrictByNameEn(String nameEn) {
        District district = districtRepository.findByDistrictEnAndIsDeletedFalse(nameEn)
            .orElseThrow(() -> new RuntimeException("District not found with name: " + nameEn));
        return districtMapper.toResponse(district);
    }

    @Override
    @Transactional(readOnly = true)
    public DistrictResponse getDistrictByNameKh(String nameKh) {
        District district = districtRepository.findByDistrictKhAndIsDeletedFalse(nameKh)
            .orElseThrow(() -> new RuntimeException("District not found with Khmer name: " + nameKh));
        return districtMapper.toResponse(district);
    }

    @Override
    @Transactional
    public DistrictResponse updateDistrict(UUID id, DistrictRequest request) {
        log.info("Updating district: {}", id);
        
        District district = districtRepository.findByIdAndIsDeletedFalse(id)
            .orElseThrow(() -> new RuntimeException("District not found"));
        
        if (request.getProvinceCode() != null && 
            !request.getProvinceCode().equals(district.getProvinceCode())) {
            if (!provinceRepository.existsByProvinceCodeAndIsDeletedFalse(request.getProvinceCode())) {
                throw new ValidationException("Province code does not exist: " + request.getProvinceCode());
            }
        }
        
        districtMapper.updateEntity(request, district);
        districtRepository.save(district);
        
        // Fetch updated district with province
        District updatedDistrict = districtRepository.findByIdAndIsDeletedFalse(id)
            .orElseThrow(() -> new RuntimeException("District not found"));
        
        log.info("District updated: {}", updatedDistrict.getDistrictCode());
        return districtMapper.toResponse(updatedDistrict);
    }

    @Override
    @Transactional
    public void deleteDistrict(UUID id) {
        District district = districtRepository.findByIdAndIsDeletedFalse(id)
            .orElseThrow(() -> new RuntimeException("District not found"));
        
        district.softDelete();
        districtRepository.save(district);
        log.info("District deleted: {}", district.getDistrictCode());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DistrictResponse> getDistrictsByProvinceCode(String provinceCode) {
        List<District> districts = districtRepository
            .findAllByProvinceCodeAndIsDeletedFalse(provinceCode);
        return districtMapper.toResponseList(districts);
    }
}
