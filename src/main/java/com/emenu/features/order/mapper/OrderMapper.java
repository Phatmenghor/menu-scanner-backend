package com.emenu.features.order.mapper;

import com.emenu.features.location.mapper.LocationMapper;
import com.emenu.features.order.dto.helper.OrderCreateHelper;
import com.emenu.features.order.dto.helper.OrderItemCreateHelper;
import com.emenu.features.order.dto.request.OrderCreateRequest;
import com.emenu.features.order.dto.response.OrderResponse;
import com.emenu.features.order.dto.response.OrderStatusHistoryResponse;
import com.emenu.features.order.dto.response.OrderStatusHistoryUserInfo;
import com.emenu.features.order.dto.response.OrderPaymentInfo;
import com.emenu.features.order.dto.response.OrderPricingInfo;
import com.emenu.features.order.models.CartItem;
import com.emenu.features.order.models.Order;
import com.emenu.features.order.models.OrderItem;
import com.emenu.features.order.models.OrderStatusHistory;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {LocationMapper.class, DeliveryOptionMapper.class, OrderItemMapper.class, PaginationMapper.class, OrderProcessStatusMapper.class, OrderStatusHistoryMapper.class})
public interface OrderMapper {

    @Mapping(target = "customerName", expression = "java(order.getCustomerIdentifier())")
    @Mapping(target = "customerPhone", expression = "java(order.getCustomerContact())")
    @Mapping(source = "business.name", target = "businessName")
    @Mapping(target = "deliveryAddress", expression = "java(mapDeliveryAddress(order))")
    @Mapping(target = "deliveryOption", expression = "java(mapDeliveryOption(order))")
    @Mapping(target = "orderProcessStatus", expression = "java(mapOrderProcessStatus(order))")
    @Mapping(target = "pricing", expression = "java(mapPricingInfo(order))")
    @Mapping(target = "statusHistory", expression = "java(mapStatusHistory(order))")
    @Mapping(target = "payment", expression = "java(mapPaymentInfo(order))")
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
                .paymentMethod(request.getPayment() != null ? request.getPayment().getPaymentMethod() : null)
                .paymentStatus(request.getPayment() != null ? request.getPayment().getPaymentStatus() : null)
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
        // Get promotion details from product or productSize
        String promotionType = null;
        BigDecimal promotionValue = null;
        LocalDateTime promotionFromDate = null;
        LocalDateTime promotionToDate = null;

        if (cartItem.getProduct() != null && cartItem.getProduct().getHasActivePromotion()) {
            promotionType = cartItem.getProduct().getPromotionType() != null ?
                    cartItem.getProduct().getPromotionType().toString() : null;
            promotionValue = cartItem.getProduct().getPromotionValue();
            promotionFromDate = cartItem.getProduct().getPromotionFromDate();
            promotionToDate = cartItem.getProduct().getPromotionToDate();
        }

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
                // Promotion details snapshot
                .promotionType(promotionType)
                .promotionValue(promotionValue)
                .promotionFromDate(promotionFromDate)
                .promotionToDate(promotionToDate)
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
     * Map current order process status with details of who set it
     * Returns the most recent status from history
     */
    default com.emenu.features.order.dto.response.OrderStatusDto mapOrderProcessStatus(Order order) {
        if (order.getStatusHistory() == null || order.getStatusHistory().isEmpty()) {
            return null;
        }

        // Get the most recent status change (last in list)
        OrderStatusHistory latestStatus = order.getStatusHistory().get(order.getStatusHistory().size() - 1);

        return com.emenu.features.order.dto.response.OrderStatusDto.builder()
                .name(latestStatus.getOrderProcessStatus() != null ?
                        latestStatus.getOrderProcessStatus().getName() : null)
                .description(latestStatus.getOrderProcessStatus() != null ?
                        latestStatus.getOrderProcessStatus().getDescription() : null)
                .changedBy(mapStatusHistoryUserInfo(latestStatus))
                .createdAt(latestStatus.getCreatedAt())
                .build();
    }

    /**
     * Calculate total number of items in the order
     */
    default Integer calculateTotalItems(Order order) {
        if (order.getItems() == null || order.getItems().isEmpty()) {
            return 0;
        }
        return order.getItems().stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();
    }

    /**
     * Map order status history to response DTOs
     */
    default List<OrderStatusHistoryResponse> mapStatusHistory(Order order) {
        if (order.getStatusHistory() == null || order.getStatusHistory().isEmpty()) {
            return null;
        }

        return order.getStatusHistory().stream()
                .map(history -> OrderStatusHistoryResponse.builder()
                        .id(history.getId())
                        .statusName(history.getOrderProcessStatus() != null ?
                                history.getOrderProcessStatus().getName() : null)
                        .statusDescription(history.getOrderProcessStatus() != null ?
                                history.getOrderProcessStatus().getDescription() : null)
                        .note(history.getNote())
                        .changedBy(mapStatusHistoryUserInfo(history))
                        .changedAt(history.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Map user details from status history
     */
    default OrderStatusHistoryUserInfo mapStatusHistoryUserInfo(OrderStatusHistory history) {
        if (history.getChangedByUser() == null) {
            return null;
        }

        return OrderStatusHistoryUserInfo.builder()
                .userId(history.getChangedByUserId())
                .firstName(history.getChangedByUser().getFirstName())
                .lastName(history.getChangedByUser().getLastName())
                .phoneNumber(history.getChangedByUser().getPhoneNumber())
                .businessId(history.getChangedByUser().getBusinessId())
                .build();
    }

    /**
     * Map pricing details to nested pricing info object
     */
    default OrderPricingInfo mapPricingInfo(Order order) {
        if (order == null) {
            return null;
        }

        BigDecimal subtotalBeforeDiscount = calculateSubtotalBeforeDiscount(order);
        BigDecimal discount = order.getDiscountAmount() != null ? order.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal subtotalAfterDiscount = subtotalBeforeDiscount.subtract(discount);

        return OrderPricingInfo.builder()
                .totalItems(calculateTotalItems(order))
                .subtotalBeforeDiscount(subtotalBeforeDiscount)
                .subtotal(subtotalAfterDiscount)
                .totalDiscount(discount)
                .deliveryFee(order.getDeliveryFee())
                .taxAmount(order.getTaxAmount())
                .finalTotal(order.getTotalAmount())
                .build();
    }

    /**
     * Calculate subtotal before any discounts by summing items at original price (currentPrice * quantity)
     */
    default BigDecimal calculateSubtotalBeforeDiscount(Order order) {
        if (order == null || order.getItems() == null || order.getItems().isEmpty()) {
            return BigDecimal.ZERO;
        }

        return order.getItems().stream()
                .map(item -> {
                    if (item.getCurrentPrice() != null && item.getQuantity() != null) {
                        return item.getCurrentPrice().multiply(new BigDecimal(item.getQuantity()));
                    }
                    return BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Map payment method and status to nested payment info object
     */
    default OrderPaymentInfo mapPaymentInfo(Order order) {
        if (order == null) {
            return null;
        }

        return OrderPaymentInfo.builder()
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
                .build();
    }
}
