package com.emenu.features.order.mapper;

import com.emenu.features.order.dto.response.OrderItemResponse;
import com.emenu.features.order.models.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderItemMapper {

    @Mapping(target = "product", expression = "java(mapProductInfo(orderItem))")
    @Mapping(source = "hasPromotion", target = "hasActivePromotion")
    @Mapping(target = "totalBeforeDiscount", expression = "java(calculateTotalBeforeDiscount(orderItem))")
    @Mapping(target = "discountAmount", expression = "java(calculateDiscountAmount(orderItem))")
    OrderItemResponse toResponse(OrderItem orderItem);

    List<OrderItemResponse> toResponseList(List<OrderItem> orderItems);

    default OrderItemResponse.OrderItemProductInfo mapProductInfo(OrderItem orderItem) {
        if (orderItem.getProduct() == null) {
            return null;
        }

        OrderItemResponse.OrderItemProductInfo info = new OrderItemResponse.OrderItemProductInfo();
        info.setId(orderItem.getProduct().getId());
        info.setName(orderItem.getProductName());
        info.setImageUrl(orderItem.getProductImageUrl());
        info.setSizeId(orderItem.getProductSizeId());
        info.setSizeName(orderItem.getSizeName());
        if (orderItem.getProduct().getStatus() != null) {
            info.setStatus(orderItem.getProduct().getStatus().toString());
        }
        return info;
    }

    default BigDecimal calculateTotalBeforeDiscount(OrderItem orderItem) {
        if (orderItem.getCurrentPrice() == null || orderItem.getQuantity() == null) {
            return BigDecimal.ZERO;
        }
        return orderItem.getCurrentPrice().multiply(new BigDecimal(orderItem.getQuantity()));
    }

    default BigDecimal calculateDiscountAmount(OrderItem orderItem) {
        BigDecimal totalBeforeDiscount = calculateTotalBeforeDiscount(orderItem);
        BigDecimal totalAfterDiscount = orderItem.getTotalPrice() != null ? orderItem.getTotalPrice() : BigDecimal.ZERO;
        return totalBeforeDiscount.subtract(totalAfterDiscount);
    }
}