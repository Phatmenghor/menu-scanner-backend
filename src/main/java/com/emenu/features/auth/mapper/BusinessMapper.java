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

@Mapper(componentModel = "spring", uses = {PaginationMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BusinessMapper {

    @Mapping(target = "hasActiveSubscription", expression = "java(business.hasActiveSubscription())")
    BusinessResponse toResponse(Business business);

    Business toEntity(BusinessCreateRequest request);

    List<BusinessResponse> toResponseList(List<Business> businesses);

    default PaginationResponse<BusinessResponse> toPaginationResponse(Page<Business> page, PaginationMapper paginationMapper) {
        return paginationMapper.toPaginationResponse(page, this::toResponseList);
    }
}
