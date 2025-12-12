package com.emenu.features.location.mapper;

import com.emenu.features.location.dto.request.DistrictRequest;
import com.emenu.features.location.dto.response.DistrictResponse;
import com.emenu.features.location.models.District;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class DistrictMapper {
    @Autowired
    protected PaginationMapper paginationMapper;
    
    @Mapping(source = "province", target = "province")
    public abstract DistrictResponse toResponse(District district);
    
    public abstract District toEntity(DistrictRequest request);
    public abstract List<DistrictResponse> toResponseList(List<District> districts);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract void updateEntity(DistrictRequest request, @MappingTarget District district);
    
    public PaginationResponse<DistrictResponse> toPaginationResponse(Page<District> page) {
        return paginationMapper.toPaginationResponse(page, this::toResponseList);
    }
}