package com.emenu.features.location.mapper;

import com.emenu.features.location.dto.request.VillageRequest;
import com.emenu.features.location.dto.response.VillageResponse;
import com.emenu.features.location.models.Village;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class VillageMapper {
    @Autowired
    protected PaginationMapper paginationMapper;
    
    @Mapping(source = "commune", target = "commune")
    @Mapping(source = "commune.district", target = "district")
    @Mapping(source = "commune.district.province", target = "province")
    public abstract VillageResponse toResponse(Village village);
    
    public abstract Village toEntity(VillageRequest request);
    public abstract List<VillageResponse> toResponseList(List<Village> villages);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract void updateEntity(VillageRequest request, @MappingTarget Village village);
    
    public PaginationResponse<VillageResponse> toPaginationResponse(Page<Village> page) {
        return paginationMapper.toPaginationResponse(page, this::toResponseList);
    }
}