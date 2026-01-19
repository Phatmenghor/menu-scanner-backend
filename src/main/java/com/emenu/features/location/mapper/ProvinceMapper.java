package com.emenu.features.location.mapper;

import com.emenu.features.location.dto.request.ProvinceRequest;
import com.emenu.features.location.dto.response.ProvinceResponse;
import com.emenu.features.location.models.Province;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", uses = {PaginationMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProvinceMapper {

    ProvinceResponse toResponse(Province province);
    Province toEntity(ProvinceRequest request);
    List<ProvinceResponse> toResponseList(List<Province> provinces);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(ProvinceRequest request, @MappingTarget Province province);

    default PaginationResponse<ProvinceResponse> toPaginationResponse(Page<Province> page, PaginationMapper paginationMapper) {
        return paginationMapper.toPaginationResponse(page, this::toResponseList);
    }
}