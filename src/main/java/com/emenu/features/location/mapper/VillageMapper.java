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

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {CommuneMapper.class, PaginationMapper.class})
public interface VillageMapper {

    @Mapping    VillageResponse toResponse(Village village);

    Village toEntity(VillageRequest request);

    List<VillageResponse> toResponseList(List<Village> villages);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(VillageRequest request, @MappingTarget Village village);

    default PaginationResponse<VillageResponse> toPaginationResponse(Page<Village> page, PaginationMapper paginationMapper) {
        return paginationMapper.toPaginationResponse(page, this::toResponseList);
    }
}