package com.emenu.features.order.mapper;

import com.emenu.features.order.dto.request.ExchangeRateCreateRequest;
import com.emenu.features.order.dto.response.ExchangeRateResponse;
import com.emenu.features.order.dto.update.ExchangeRateUpdateRequest;
import com.emenu.features.order.models.ExchangeRate;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", uses = {PaginationMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ExchangeRateMapper {

    @Mapping(target = "isActive", constant = "true")
    ExchangeRate toEntity(ExchangeRateCreateRequest request);

    ExchangeRateResponse toResponse(ExchangeRate exchangeRate);

    List<ExchangeRateResponse> toResponseList(List<ExchangeRate> exchangeRates);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(ExchangeRateUpdateRequest request, @MappingTarget ExchangeRate exchangeRate);

    default PaginationResponse<ExchangeRateResponse> toPaginationResponse(Page<ExchangeRate> exchangeRatePage, PaginationMapper paginationMapper) {
return paginationMapper.toPaginationResponse(exchangeRatePage, this::toResponseList);
    }
}