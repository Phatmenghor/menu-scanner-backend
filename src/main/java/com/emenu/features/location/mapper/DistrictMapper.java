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

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {ProvinceMapper.class, PaginationMapper.class})
public interface DistrictMapper {

    @Mapping(target = "province", source = "province")
    DistrictResponse toResponse(District district);

    District toEntity(DistrictRequest request);

    List<DistrictResponse> toResponseList(List<District> districts);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(DistrictRequest request, @MappingTarget District district);

    default PaginationResponse<DistrictResponse> toPaginationResponse(Page<District> page, PaginationMapper paginationMapper) {
        return paginationMapper.toPaginationResponse(page, this::toResponseList);
    }
}