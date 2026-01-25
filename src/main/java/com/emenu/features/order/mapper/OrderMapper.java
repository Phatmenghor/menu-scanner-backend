package com.emenu.features.order.mapper;

import com.emenu.enums.payment.PaymentMethod;
import com.emenu.features.location.mapper.CustomerAddressMapper;
import com.emenu.features.main.models.Product;
import com.emenu.features.order.dto.helper.OrderCreateHelper;
import com.emenu.features.order.dto.helper.OrderItemCreateHelper;
import com.emenu.features.order.dto.request.OrderCreateRequest;
import com.emenu.features.order.dto.request.OrderItemRequest;
import com.emenu.features.order.dto.request.POSOrderCreateRequest;
import com.emenu.features.order.dto.response.OrderResponse;
import com.emenu.features.order.models.Cart;
import com.emenu.features.order.models.CartItem;
import com.emenu.features.order.models.Order;
import com.emenu.features.order.models.OrderItem;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {CustomerAddressMapper.class, DeliveryOptionMapper.class, OrderItemMapper.class, PaginationMapper.class})
public interface OrderMapper {

    @Mapping(target = "customerName", expression = "java(order.getCustomerIdentifier())")
    @Mapping(target = "customerPhone", expression = "java(order.getCustomerContact())")
    @Mapping(source = "guestLocation", target = "customerLocation")
    @Mapping(source = "business.name", target = "businessName")
    @Mapping(target = "canBeModified", expression = "java(order.canBeModified())")
    @Mapping(target = "canBeCancelled", expression = "java(order.canBeCancelled())")
    @Mapping(target = "formattedAmount", expression = "java(formatAmount(order.getTotalAmount()))")
    OrderResponse toResponse(Order order);

    List<OrderResponse> toResponseList(List<Order> orders);

    default String formatAmount(java.math.BigDecimal amount) {
        if (amount == null) return "$0.00";
        return String.format("$%.2f", amount);
    }

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
     * Update order with guest details - pure MapStruct mapping
     */
    @Mapping(target = "isGuestOrder", constant = "true")
    @Mapping(source = "guestPhone", target = "guestPhone")
    @Mapping(source = "guestName", target = "guestName")
    @Mapping(source = "guestLocation", target = "guestLocation")
    void updateWithGuestDetails(@MappingTarget Order order, String guestPhone, String guestName, String guestLocation);

    /**
     * Helper to build OrderCreateHelper for base order
     */
    default OrderCreateHelper buildBaseOrderHelper(OrderCreateRequest request, UUID customerId, String orderNumber) {
        return OrderCreateHelper.builder()
                .orderNumber(orderNumber)
                .customerId(customerId)
                .businessId(request.getBusinessId())
                .deliveryAddressId(request.getDeliveryAddressId())
                .deliveryOptionId(request.getDeliveryOptionId())
                .paymentMethod(request.getPaymentMethod())
                .customerNote(request.getCustomerNote())
                .isPosOrder(request.getIsPosOrder())
                .isGuestOrder(request.getIsGuestOrder())
                .build();
    }

    /**
     * Helper to build OrderCreateHelper for POS order
     */
    default OrderCreateHelper buildPOSOrderHelper(POSOrderCreateRequest request, UUID businessId, String orderNumber, BigDecimal subtotal) {
        return OrderCreateHelper.builder()
                .orderNumber(orderNumber)
                .businessId(businessId)
                .guestPhone(request.getCustomerPhone())
                .guestName(request.getCustomerName())
                .guestLocation(request.getCustomerLocation())
                .paymentMethod(request.getPaymentMethod())
                .customerNote(request.getCustomerNote())
                .businessNote(request.getBusinessNote())
                .isPosOrder(true)
                .isGuestOrder(true)
                .isPaid(true)
                .subtotal(subtotal)
                .totalAmount(subtotal)
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

    /**
     * Helper to build OrderItemCreateHelper from request
     */
    default OrderItemCreateHelper buildOrderItemHelperFromRequest(OrderItemRequest itemRequest, UUID orderId, Product product,
                                                                    String sizeName, BigDecimal unitPrice) {
        return OrderItemCreateHelper.builder()
                .orderId(orderId)
                .productId(itemRequest.getProductId())
                .productSizeId(itemRequest.getProductSizeId())
                .productName(product.getName())
                .productImageUrl(product.getMainImageUrl())
                .quantity(itemRequest.getQuantity())
                .sizeName(sizeName)
                .unitPrice(unitPrice)
                .build();
    }
}