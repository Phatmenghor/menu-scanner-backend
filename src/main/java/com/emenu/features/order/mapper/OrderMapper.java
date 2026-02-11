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
        var builder = OrderCreateHelper.builder()
                .orderNumber(orderNumber)
                .customerId(customerId)
                .businessId(request.getBusinessId())
                .paymentMethod(request.getPaymentMethod())
                .customerNote(request.getCustomerNote());

        // Build delivery address snapshot from frontend full object
        if (request.getDeliveryAddress() != null) {
            var addr = request.getDeliveryAddress();
            String addressSnapshot = String.format("%s, %s, %s, %s",
                    addr.getVillage() != null ? addr.getVillage() : "",
                    addr.getCommune() != null ? addr.getCommune() : "",
                    addr.getDistrict() != null ? addr.getDistrict() : "",
                    addr.getProvince() != null ? addr.getProvince() : "")
                    .replaceAll("^, |, $", "");
            builder.deliveryAddressSnapshot(addressSnapshot);
        }

        // Build delivery option snapshot from frontend full object
        if (request.getDeliveryOption() != null) {
            var option = request.getDeliveryOption();
            builder.deliveryOptionName(option.getName())
                    .deliveryOptionDescription(option.getDescription())
                    .deliveryFee(option.getPrice());
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
     * Map delivery address snapshot JSON to OrderDeliveryAddressDto
     */
    default com.emenu.features.order.dto.response.OrderDeliveryAddressDto mapDeliveryAddress(Order order) {
        if (order.getDeliveryAddressSnapshot() == null || order.getDeliveryAddressSnapshot().isBlank()) {
            return null;
        }

        try {
            // Parse JSON string to Map
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> addressMap = objectMapper.readValue(
                    order.getDeliveryAddressSnapshot(),
                    java.util.Map.class
            );

            // Build clean DTO with only the fields we need
            return com.emenu.features.order.dto.response.OrderDeliveryAddressDto.builder()
                    .street((String) addressMap.get("street"))
                    .city((String) addressMap.get("city"))
                    .district((String) addressMap.get("district"))
                    .commune((String) addressMap.get("commune"))
                    .postalCode((String) addressMap.get("postalCode"))
                    .country((String) addressMap.get("country"))
                    .build();
        } catch (Exception e) {
            // If JSON parsing fails, return null (backward compatibility with old data)
            return null;
        }
    }

    /**
     * Map delivery option fields to OrderDeliveryOptionDto
     */
    default com.emenu.features.order.dto.response.OrderDeliveryOptionDto mapDeliveryOption(Order order) {
        if (order.getDeliveryOptionName() == null || order.getDeliveryOptionName().isBlank()) {
            return null;
        }

        return com.emenu.features.order.dto.response.OrderDeliveryOptionDto.builder()
                .name(order.getDeliveryOptionName())
                .description(order.getDeliveryOptionDescription())
                .price(order.getDeliveryFee())
                .build();
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
