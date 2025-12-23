package com.emenu.features.payment.mapper;

import com.emenu.features.payment.dto.request.BusinessExchangeRateCreateRequest;
import com.emenu.features.payment.dto.response.BusinessExchangeRateResponse;
import com.emenu.features.payment.dto.update.BusinessExchangeRateUpdateRequest;
import com.emenu.features.payment.models.BusinessExchangeRate;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class BusinessExchangeRateMapper {

    @Autowired
    protected PaginationMapper paginationMapper;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    public abstract BusinessExchangeRate toEntity(BusinessExchangeRateCreateRequest request);

    @Mapping(source = "business.name", target = "businessName")
    @Mapping(target = "formattedKhrRate", expression = "java(exchangeRate.getFormattedKhrRate())")
    @Mapping(target = "formattedCnyRate", expression = "java(exchangeRate.getFormattedCnyRate())")
    @Mapping(target = "formattedThbRate", expression = "java(exchangeRate.getFormattedThbRate())")
    @Mapping(target = "formattedVndRate", expression = "java(exchangeRate.getFormattedVndRate())")
    public abstract BusinessExchangeRateResponse toResponse(BusinessExchangeRate exchangeRate);

    public abstract List<BusinessExchangeRateResponse> toResponseList(List<BusinessExchangeRate> exchangeRates);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "businessId", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    public abstract void updateEntity(BusinessExchangeRateUpdateRequest request, @MappingTarget BusinessExchangeRate exchangeRate);

    public PaginationResponse<BusinessExchangeRateResponse> toPaginationResponse(Page<BusinessExchangeRate> exchangeRatePage) {
        return paginationMapper.toPaginationResponse(exchangeRatePage, this::toResponseList);
    }
}