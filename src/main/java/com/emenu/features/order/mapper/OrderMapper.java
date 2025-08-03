package com.emenu.features.order.mapper;

import com.emenu.features.customer.mapper.CustomerAddressMapper;
import com.emenu.features.order.dto.response.OrderItemResponse;
import com.emenu.features.order.dto.response.OrderResponse;
import com.emenu.features.order.models.Order;
import com.emenu.features.order.models.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {CustomerAddressMapper.class, DeliveryOptionMapper.class})
public abstract class OrderMapper {

    @Mapping(source = "customer.firstName", target = "customerName", qualifiedByName = "getCustomerName")
    @Mapping(source = "business.name", target = "businessName")
    @Mapping(target = "canBeModified", expression = "java(order.canBeModified())")
    @Mapping(target = "canBeCancelled", expression = "java(order.canBeCancelled())")
    public abstract OrderResponse toResponse(Order order);

    public abstract List<OrderResponse> toResponseList(List<Order> orders);

    public abstract OrderItemResponse toItemResponse(OrderItem orderItem);

    public abstract List<OrderItemResponse> toItemResponseList(List<OrderItem> orderItems);

    @Named("getCustomerName")
    protected String getCustomerName(com.emenu.features.auth.models.User customer) {
        if (customer == null) return null;
        return customer.getFullName();
    }
}