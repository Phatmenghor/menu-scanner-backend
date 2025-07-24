package com.emenu.features.auth.mapper;

import com.emenu.features.auth.dto.request.ExchangeRateCreateRequest;
import com.emenu.features.auth.dto.response.ExchangeRateResponse;
import com.emenu.features.auth.dto.update.ExchangeRateUpdateRequest;
import com.emenu.features.auth.models.ExchangeRate;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class ExchangeRateMapper {

    @Autowired
    protected PaginationMapper paginationMapper;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    public abstract ExchangeRate toEntity(ExchangeRateCreateRequest request);

    @Mapping(target = "formattedRate", expression = "java(exchangeRate.getFormattedRate())")
    @Mapping(target = "displayName", expression = "java(exchangeRate.getDisplayName())")
    public abstract ExchangeRateResponse toResponse(ExchangeRate exchangeRate);

    public abstract List<ExchangeRateResponse> toResponseList(List<ExchangeRate> exchangeRates);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    public abstract void updateEntity(ExchangeRateUpdateRequest request, @MappingTarget ExchangeRate exchangeRate);

    @AfterMapping
    protected void setCalculatedFields(@MappingTarget ExchangeRateResponse response, ExchangeRate exchangeRate) {
        response.setFormattedRate(exchangeRate.getFormattedRate());
        response.setDisplayName(exchangeRate.getDisplayName());
    }

    public PaginationResponse<ExchangeRateResponse> toPaginationResponse(Page<ExchangeRate> exchangeRatePage) {
        return paginationMapper.toPaginationResponse(exchangeRatePage, this::toResponseList);
    }
}