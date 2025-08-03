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


    @Mapping(source = "customer", target = "customerName", qualifiedByName = "getCustomerName")
    @Mapping(source = "business.name", target = "businessName")
    @Mapping(target = "canBeModified", expression = "java(order.canBeModified())")
    @Mapping(target = "canBeCancelled", expression = "java(order.canBeCancelled())")
    public abstract OrderResponse toResponse(Order order);

    public abstract List<OrderResponse> toResponseList(List<Order> orders);

    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "product.images", target = "productImageUrl", qualifiedByName = "getMainImageUrl")
    @Mapping(source = "productSize.name", target = "sizeName")
    public abstract OrderItemResponse toItemResponse(OrderItem orderItem);

    public abstract List<OrderItemResponse> toItemResponseList(List<OrderItem> orderItems);

    @Named("getCustomerName")
    protected String getCustomerName(com.emenu.features.auth.models.User customer) {
        if (customer == null) return null;
        return customer.getFullName();
    }

    @Named("getMainImageUrl")
    protected String getMainImageUrl(List<com.emenu.features.product.models.ProductImage> images) {
        if (images == null || images.isEmpty()) return null;
        return images.stream()
                .filter(img -> img.getImageType() == com.emenu.enums.product.ImageType.MAIN)
                .findFirst()
                .map(com.emenu.features.product.models.ProductImage::getImageUrl)
                .orElse(images.get(0).getImageUrl());
    }
}