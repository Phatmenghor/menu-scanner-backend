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
        uses = {DistrictMapper.class, PaginationMapper.class})
public interface CommuneMapper {

    @Mapping(target = "district", source = "district")
    CommuneResponse toResponse(Commune commune);

    Commune toEntity(CommuneRequest request);

    List<CommuneResponse> toResponseList(List<Commune> communes);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(CommuneRequest request, @MappingTarget Commune commune);

    default PaginationResponse<CommuneResponse> toPaginationResponse(Page<Commune> page, PaginationMapper paginationMapper) {
        return paginationMapper.toPaginationResponse(page, this::toResponseList);
    }
}