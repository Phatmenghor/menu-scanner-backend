package com.emenu.features.location.mapper;

import com.emenu.features.location.dto.request.LocationCreateRequest;
import com.emenu.features.location.dto.response.LocationResponse;
import com.emenu.features.location.dto.update.LocationUpdateRequest;
import com.emenu.features.location.models.Location;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", uses = {PaginationMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LocationMapper {

    Location toEntity(LocationCreateRequest request);

    @Mapping(target = "fullAddress", expression = "java(address.getFullAddress())")
    @Mapping(target = "hasCoordinates", expression = "java(address.hasCoordinates())")
    LocationResponse toResponse(Location address);

    List<LocationResponse> toResponseList(List<Location> addresses);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(LocationUpdateRequest request, @MappingTarget Location address);

    default PaginationResponse<LocationResponse> toPaginationResponse(Page<Location> addresses, PaginationMapper paginationMapper) {
return paginationMapper.toPaginationResponse(addresses, this::toResponseList);
    }
}