package com.emenu.features.location.mapper;

import com.emenu.features.location.dto.request.CommuneRequest;
import com.emenu.features.location.dto.response.CommuneResponse;
import com.emenu.features.location.models.Commune;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {DistrictMapper.class})  // Use DistrictMapper for nested district
public abstract class CommuneMapper {
    @Autowired
    protected PaginationMapper paginationMapper;
    
    @Autowired
    protected DistrictMapper districtMapper;
    
    // Map commune with its district (district will include province automatically)
    @Mapping(target = "district", source = "district")
    public abstract CommuneResponse toResponse(Commune commune);
    
    public abstract Commune toEntity(CommuneRequest request);
    
    public abstract List<CommuneResponse> toResponseList(List<Commune> communes);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract void updateEntity(CommuneRequest request, @MappingTarget Commune commune);
    
    public PaginationResponse<CommuneResponse> toPaginationResponse(Page<Commune> page) {
        return paginationMapper.toPaginationResponse(page, this::toResponseList);
    }
}