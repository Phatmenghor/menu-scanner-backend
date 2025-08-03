package com.emenu.features.order.mapper;

import com.emenu.features.customer.mapper.CustomerAddressMapper;
import com.emenu.features.order.dto.response.OrderResponse;
import com.emenu.features.order.models.Order;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {CustomerAddressMapper.class, DeliveryOptionMapper.class, OrderItemMapper.class})
public abstract class OrderMapper {

    @Autowired
    protected PaginationMapper paginationMapper;

    @Mapping(target = "customerName", expression = "java(order.getCustomerIdentifier())")
    @Mapping(target = "customerPhone", expression = "java(order.getCustomerContact())")
    @Mapping(source = "guestLocation", target = "customerLocation")
    @Mapping(source = "business.name", target = "businessName")
    @Mapping(target = "canBeModified", expression = "java(order.canBeModified())")
    @Mapping(target = "canBeCancelled", expression = "java(order.canBeCancelled())")
    @Mapping(target = "formattedAmount", expression = "java(formatAmount(order.getTotalAmount()))")
    public abstract OrderResponse toResponse(Order order);

    public abstract List<OrderResponse> toResponseList(List<Order> orders);

    protected String formatAmount(java.math.BigDecimal amount) {
        if (amount == null) return "$0.00";
        return String.format("$%.2f", amount);
    }

    public PaginationResponse<OrderResponse> toPaginationResponse(Page<Order> orderPage) {
        return paginationMapper.toPaginationResponse(orderPage, this::toResponseList);
    }
}