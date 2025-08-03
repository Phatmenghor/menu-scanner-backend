package com.emenu.features.payment.mapper;

import com.emenu.features.payment.dto.request.ExchangeRateCreateRequest;
import com.emenu.features.payment.dto.response.ExchangeRateResponse;
import com.emenu.features.payment.dto.update.ExchangeRateUpdateRequest;
import com.emenu.features.payment.models.ExchangeRate;
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
    public abstract ExchangeRate toEntity(ExchangeRateCreateRequest request);

    public abstract ExchangeRateResponse toResponse(ExchangeRate exchangeRate);

    public abstract List<ExchangeRateResponse> toResponseList(List<ExchangeRate> exchangeRates);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    public abstract void updateEntity(ExchangeRateUpdateRequest request, @MappingTarget ExchangeRate exchangeRate);

    public PaginationResponse<ExchangeRateResponse> toPaginationResponse(Page<ExchangeRate> exchangeRatePage) {
        return paginationMapper.toPaginationResponse(exchangeRatePage, this::toResponseList);
    }
}