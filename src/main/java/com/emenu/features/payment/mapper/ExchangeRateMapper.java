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

@Mapper(componentModel = "spring", uses = {PaginationMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ExchangeRateMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    ExchangeRate toEntity(ExchangeRateCreateRequest request);

    ExchangeRateResponse toResponse(ExchangeRate exchangeRate);

    List<ExchangeRateResponse> toResponseList(List<ExchangeRate> exchangeRates);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateEntity(ExchangeRateUpdateRequest request, @MappingTarget ExchangeRate exchangeRate);

    default PaginationResponse<ExchangeRateResponse> toPaginationResponse(Page<ExchangeRate> exchangeRatePage, PaginationMapper paginationMapper) {
        return paginationMapper.toPaginationResponse(exchangeRatePage, this::toResponseList);
    }
}