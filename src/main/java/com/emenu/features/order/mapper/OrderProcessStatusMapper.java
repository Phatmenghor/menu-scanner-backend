package com.emenu.features.order.mapper;

import com.emenu.features.order.dto.request.OrderProcessStatusCreateRequest;
import com.emenu.features.order.dto.response.OrderProcessStatusResponse;
import com.emenu.features.order.dto.update.OrderProcessStatusUpdateRequest;
import com.emenu.features.order.models.OrderProcessStatus;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", uses = {PaginationMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderProcessStatusMapper {

    OrderProcessStatus toEntity(OrderProcessStatusCreateRequest request);

    @Mapping(source = "business.name", target = "businessName")
    OrderProcessStatusResponse toResponse(OrderProcessStatus orderProcessStatus);

    List<OrderProcessStatusResponse> toResponseList(List<OrderProcessStatus> orderProcessStatuses);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(OrderProcessStatusUpdateRequest request, @MappingTarget OrderProcessStatus orderProcessStatus);

    default PaginationResponse<OrderProcessStatusResponse> toPaginationResponse(
            Page<OrderProcessStatus> page, PaginationMapper paginationMapper) {
        return paginationMapper.toPaginationResponse(page, this::toResponseList);
    }
}
