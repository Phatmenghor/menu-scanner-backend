package com.emenu.features.auth.mapper;

import com.emenu.features.auth.dto.request.BusinessCreateRequest;
import com.emenu.features.auth.dto.response.BusinessResponse;
import com.emenu.features.auth.models.Business;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class BusinessMapper {

    @Autowired
    protected PaginationMapper paginationMapper;

    @Mapping(target = "hasActiveSubscription", expression = "java(business.hasActiveSubscription())")
    public abstract BusinessResponse toResponse(Business business);
    
    public abstract Business toEntity(BusinessCreateRequest request);

    public abstract List<BusinessResponse> toResponseList(List<Business> businesses);

    public PaginationResponse<BusinessResponse> toPaginationResponse(Page<Business> page) {
        return paginationMapper.toPaginationResponse(page, this::toResponseList);
    }
}
