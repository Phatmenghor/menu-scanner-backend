package com.emenu.features.order.mapper;

import com.emenu.features.order.dto.response.OrderItemResponse;
import com.emenu.features.order.models.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderItemMapper {

    @Mapping(target = "formattedUnitPrice", expression = "java(formatAmount(orderItem.getUnitPrice()))")
    @Mapping(target = "formattedTotalPrice", expression = "java(formatAmount(orderItem.getTotalPrice()))")
    OrderItemResponse toResponse(OrderItem orderItem);

    List<OrderItemResponse> toResponseList(List<OrderItem> orderItems);

    default String formatAmount(java.math.BigDecimal amount) {
        if (amount == null) return "$0.00";
        return String.format("$%.2f", amount);
    }
}