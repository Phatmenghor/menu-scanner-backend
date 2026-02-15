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
    @Mapping(target = "deliveryAddress", expression = "java(mapDeliveryAddress(order))")
    @Mapping(target = "deliveryOption", expression = "java(mapDeliveryOption(order))")
    @Mapping(target = "orderProcessStatus", expression = "java(mapOrderProcessStatus(order))")
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
        com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

        var builder = OrderCreateHelper.builder()
                .orderNumber(orderNumber)
                .customerId(customerId)
                .businessId(request.getBusinessId())
                .paymentMethod(request.getPaymentMethod())
                .customerNote(request.getCustomerNote());

        // Serialize full delivery address object as JSON snapshot
        if (request.getDeliveryAddress() != null) {
            try {
                builder.deliveryAddressSnapshot(objectMapper.writeValueAsString(request.getDeliveryAddress()));
            } catch (Exception e) {
                builder.deliveryAddressSnapshot(null);
            }
        }

        // Serialize full delivery option object as JSON snapshot
        if (request.getDeliveryOption() != null) {
            try {
                builder.deliveryOptionSnapshot(objectMapper.writeValueAsString(request.getDeliveryOption()));
            } catch (Exception e) {
                builder.deliveryOptionSnapshot(null);
            }
            builder.deliveryFee(request.getDeliveryOption().getPrice());
        }

        return builder.build();
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
                // Pricing snapshot
                .currentPrice(cartItem.getCurrentPrice())
                .finalPrice(cartItem.getFinalPrice())
                .unitPrice(cartItem.getFinalPrice())
                .hasPromotion(cartItem.hasDiscount())
                // Note: Promotion details would need to come from Product/ProductSize
                // For now, we only capture that there IS a promotion
                .quantity(cartItem.getQuantity())
                .build();
    }

    /**
     * Deserialize delivery address JSON snapshot to OrderDeliveryAddressDto
     */
    default com.emenu.features.order.dto.response.OrderDeliveryAddressDto mapDeliveryAddress(Order order) {
        if (order.getDeliveryAddressSnapshot() == null || order.getDeliveryAddressSnapshot().isBlank()) {
            return null;
        }

        try {
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return objectMapper.readValue(
                    order.getDeliveryAddressSnapshot(),
                    com.emenu.features.order.dto.response.OrderDeliveryAddressDto.class
            );
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Deserialize delivery option JSON snapshot to OrderDeliveryOptionDto
     */
    default com.emenu.features.order.dto.response.OrderDeliveryOptionDto mapDeliveryOption(Order order) {
        if (order.getDeliveryOptionSnapshot() == null || order.getDeliveryOptionSnapshot().isBlank()) {
            return null;
        }

        try {
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return objectMapper.readValue(
                    order.getDeliveryOptionSnapshot(),
                    com.emenu.features.order.dto.response.OrderDeliveryOptionDto.class
            );
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Map order process status name snapshot to OrderStatusDto
     */
    default com.emenu.features.order.dto.response.OrderStatusDto mapOrderProcessStatus(Order order) {
        if (order.getOrderProcessStatusName() == null || order.getOrderProcessStatusName().isBlank()) {
            return null;
        }

        return com.emenu.features.order.dto.response.OrderStatusDto.builder()
                .name(order.getOrderProcessStatusName())
                .build();
    }
}
