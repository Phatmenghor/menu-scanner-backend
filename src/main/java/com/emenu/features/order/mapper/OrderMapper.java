package com.emenu.features.order.mapper;

import com.emenu.features.location.mapper.LocationMapper;
import com.emenu.features.order.dto.helper.OrderCreateHelper;
import com.emenu.features.order.dto.helper.OrderItemCreateHelper;
import com.emenu.features.order.dto.request.OrderCreateRequest;
import com.emenu.features.order.dto.response.OrderResponse;
import com.emenu.features.order.models.CartItem;
import com.emenu.features.order.models.Order;
import com.emenu.features.order.models.OrderItem;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {LocationMapper.class, DeliveryOptionMapper.class, OrderItemMapper.class, PaginationMapper.class, OrderProcessStatusMapper.class})
public interface OrderMapper {

    @Mapping(target = "customerName", expression = "java(order.getCustomerIdentifier())")
    @Mapping(target = "customerPhone", expression = "java(order.getCustomerContact())")
    @Mapping(source = "business.name", target = "businessName")
    OrderResponse toResponse(Order order);

    List<OrderResponse> toResponseList(List<Order> orders);

    default PaginationResponse<OrderResponse> toPaginationResponse(Page<Order> orderPage, PaginationMapper paginationMapper) {
        return paginationMapper.toPaginationResponse(orderPage, this::toResponseList);
    }

    /**
     * Create order from helper DTO - pure MapStruct mapping
     */
    Order createFromHelper(OrderCreateHelper helper);

    /**
     * Create order item from helper DTO - pure MapStruct mapping
     */
    OrderItem createOrderItemFromHelper(OrderItemCreateHelper helper);

    /**
     * Helper to build OrderCreateHelper for checkout order
     */
    default OrderCreateHelper buildOrderHelper(OrderCreateRequest request, UUID customerId, String orderNumber) {
        return OrderCreateHelper.builder()
                .orderNumber(orderNumber)
                .customerId(customerId)
                .businessId(request.getBusinessId())
                .deliveryAddressId(request.getDeliveryAddressId())
                .deliveryOptionId(request.getDeliveryOptionId())
                .paymentMethod(request.getPaymentMethod())
                .customerNote(request.getCustomerNote())
                .build();
    }

    /**
     * Helper to build OrderItemCreateHelper from cart item
     */
    default OrderItemCreateHelper buildOrderItemHelperFromCartItem(CartItem cartItem, UUID orderId) {
        return OrderItemCreateHelper.builder()
                .orderId(orderId)
                .productId(cartItem.getProductId())
                .productSizeId(cartItem.getProductSizeId())
                .productName(cartItem.getProduct().getName())
                .productImageUrl(cartItem.getProduct().getMainImageUrl())
                .sizeName(cartItem.getSizeName())
                .unitPrice(cartItem.getFinalPrice())
                .quantity(cartItem.getQuantity())
                .build();
    }
}
