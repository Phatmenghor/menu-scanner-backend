package com.emenu.features.order.mapper;

import com.emenu.features.order.dto.request.DeliveryOptionCreateRequest;
import com.emenu.features.order.dto.response.DeliveryOptionResponse;
import com.emenu.features.order.dto.update.DeliveryOptionUpdateRequest;
import com.emenu.features.order.models.DeliveryOption;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", uses = {PaginationMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DeliveryOptionMapper {

    @Mapping    @Mapping    @Mapping    DeliveryOption toEntity(DeliveryOptionCreateRequest request);

    @Mapping(source = "business.name", target = "businessName")
    DeliveryOptionResponse toResponse(DeliveryOption deliveryOption);

    List<DeliveryOptionResponse> toResponseList(List<DeliveryOption> deliveryOptions);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping    @Mapping    @Mapping    void updateEntity(DeliveryOptionUpdateRequest request, @MappingTarget DeliveryOption deliveryOption);

    default PaginationResponse<DeliveryOptionResponse> toPaginationResponse(Page<DeliveryOption> deliveryOptionPage, PaginationMapper paginationMapper) {
return paginationMapper.toPaginationResponse(deliveryOptionPage, this::toResponseList);
    }
}