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

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class ProvinceMapper {
    @Autowired
    protected PaginationMapper paginationMapper;
    
    public abstract ProvinceResponse toResponse(Province province);
    public abstract Province toEntity(ProvinceRequest request);
    public abstract List<ProvinceResponse> toResponseList(List<Province> provinces);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract void updateEntity(ProvinceRequest request, @MappingTarget Province province);
    
    public PaginationResponse<ProvinceResponse> toPaginationResponse(Page<Province> page) {
        return paginationMapper.toPaginationResponse(page, this::toResponseList);
    }
}