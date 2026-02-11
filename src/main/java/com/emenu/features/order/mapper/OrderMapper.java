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
     * Map delivery address snapshot string to LocationResponse
     */
    default com.emenu.features.location.dto.response.LocationResponse mapDeliveryAddress(Order order) {
        if (order.getDeliveryAddressSnapshot() == null || order.getDeliveryAddressSnapshot().isBlank()) {
            return null;
        }

        var response = new com.emenu.features.location.dto.response.LocationResponse();
        response.setFullAddress(order.getDeliveryAddressSnapshot());

        // Parse the snapshot back to individual fields if needed
        // Format is: "village, commune, district, province"
        String[] parts = order.getDeliveryAddressSnapshot().split(",\\s*");
        if (parts.length >= 4) {
            response.setVillage(parts[0].isEmpty() ? null : parts[0]);
            response.setCommune(parts[1].isEmpty() ? null : parts[1]);
            response.setDistrict(parts[2].isEmpty() ? null : parts[2]);
            response.setProvince(parts[3].isEmpty() ? null : parts[3]);
        }

        return response;
    }

    /**
     * Map delivery option fields to DeliveryOptionResponse
     */
    default com.emenu.features.order.dto.response.DeliveryOptionResponse mapDeliveryOption(Order order) {
        if (order.getDeliveryOptionName() == null || order.getDeliveryOptionName().isBlank()) {
            return null;
        }

        var response = new com.emenu.features.order.dto.response.DeliveryOptionResponse();
        response.setName(order.getDeliveryOptionName());
        response.setDescription(order.getDeliveryOptionDescription());
        response.setPrice(order.getDeliveryFee());
        response.setBusinessId(order.getBusinessId());

        return response;
    }
}
