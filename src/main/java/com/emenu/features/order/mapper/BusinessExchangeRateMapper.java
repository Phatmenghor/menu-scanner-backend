package com.emenu.features.order.mapper;

import com.emenu.features.order.dto.request.BusinessExchangeRateCreateRequest;
import com.emenu.features.order.dto.response.BusinessExchangeRateResponse;
import com.emenu.features.order.dto.update.BusinessExchangeRateUpdateRequest;
import com.emenu.features.order.models.BusinessExchangeRate;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", uses = {PaginationMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BusinessExchangeRateMapper {

    @Mapping    @Mapping(target = "isActive", constant = "true")
    BusinessExchangeRate toEntity(BusinessExchangeRateCreateRequest request);

    @Mapping(source = "business.name", target = "businessName")
    @Mapping(target = "formattedKhrRate", expression = "java(exchangeRate.getFormattedKhrRate())")
    @Mapping(target = "formattedCnyRate", expression = "java(exchangeRate.getFormattedCnyRate())")
    @Mapping(target = "formattedThbRate", expression = "java(exchangeRate.getFormattedThbRate())")
    @Mapping(target = "formattedVndRate", expression = "java(exchangeRate.getFormattedVndRate())")
    BusinessExchangeRateResponse toResponse(BusinessExchangeRate exchangeRate);

    List<BusinessExchangeRateResponse> toResponseList(List<BusinessExchangeRate> exchangeRates);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping    @Mapping    @Mapping(target = "isActive", ignore = true)
    void updateEntity(BusinessExchangeRateUpdateRequest request, @MappingTarget BusinessExchangeRate exchangeRate);

    default PaginationResponse<BusinessExchangeRateResponse> toPaginationResponse(Page<BusinessExchangeRate> exchangeRatePage, PaginationMapper paginationMapper) {
return paginationMapper.toPaginationResponse(exchangeRatePage, this::toResponseList);
    }
}