package com.emenu.features.order.mapper;

import com.emenu.features.order.dto.helper.CartCreateHelper;
import com.emenu.features.order.dto.response.CartItemProductInfo;
import com.emenu.features.order.dto.response.CartItemResponse;
import com.emenu.features.order.dto.response.CartResponse;
import com.emenu.features.order.dto.response.CartSummaryResponse;
import com.emenu.features.order.models.Cart;
import com.emenu.features.order.models.CartItem;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring", uses = {PaginationMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CartMapper {

    @Mapping(target = "product", ignore = true)
    @Mapping(target = "currentPrice", expression = "java(cartItem.getCurrentPrice())")
    @Mapping(target = "finalPrice", expression = "java(cartItem.getFinalPrice())")
    @Mapping(target = "totalPrice", expression = "java(cartItem.getTotalPrice())")
    @Mapping(target = "hasActivePromotion", expression = "java(cartItem.hasDiscount())")
    CartItemResponse toItemResponse(CartItem cartItem);

    @AfterMapping
    default void setProductInfo(@MappingTarget CartItemResponse response, CartItem cartItem) {
        CartItemProductInfo productInfo = new CartItemProductInfo();
        productInfo.setId(cartItem.getProductId());
        productInfo.setSizeId(cartItem.getProductSizeId());
        productInfo.setSizeName(cartItem.getSizeName());
        if (cartItem.getProduct() != null) {
            productInfo.setName(cartItem.getProduct().getName());
            productInfo.setImageUrl(cartItem.getProduct().getMainImageUrl());
            productInfo.setStatus(cartItem.getProduct().getStatus() != null
                    ? cartItem.getProduct().getStatus().name() : null);
        }
        response.setProduct(productInfo);
    }

    @AfterMapping
    default void setPromotionDetails(@MappingTarget CartItemResponse response, CartItem cartItem) {
        if (cartItem.getProductSize() != null && cartItem.getProductSize().isPromotionActive()) {
            response.setPromotionType(cartItem.getProductSize().getPromotionType() != null ?
                    cartItem.getProductSize().getPromotionType().name() : null);
            response.setPromotionValue(cartItem.getProductSize().getPromotionValue());
            response.setPromotionFromDate(cartItem.getProductSize().getPromotionFromDate());
            response.setPromotionToDate(cartItem.getProductSize().getPromotionToDate());
        } else if (cartItem.getProduct() != null && cartItem.getProduct().isPromotionActive()) {
            response.setPromotionType(cartItem.getProduct().getPromotionType() != null ?
                    cartItem.getProduct().getPromotionType().name() : null);
            response.setPromotionValue(cartItem.getProduct().getPromotionValue());
            response.setPromotionFromDate(cartItem.getProduct().getPromotionFromDate());
            response.setPromotionToDate(cartItem.getProduct().getPromotionToDate());
        }
    }

    /**
     * Calculate detailed pricing breakdown for standardized format across cart/checkout/order
     */
    @AfterMapping
    default void calculatePricingBreakdown(@MappingTarget CartItemResponse response, CartItem cartItem) {
        if (response.getCurrentPrice() != null && response.getQuantity() != null) {
            // totalBeforeDiscount: currentPrice * quantity
            BigDecimal totalBeforeDiscount = response.getCurrentPrice()
                    .multiply(new BigDecimal(response.getQuantity()));
            response.setTotalBeforeDiscount(totalBeforeDiscount);

            // discountAmount: totalBeforeDiscount - totalPrice
            if (response.getTotalPrice() != null) {
                BigDecimal discountAmount = totalBeforeDiscount.subtract(response.getTotalPrice());
                response.setDiscountAmount(discountAmount);
            }
        }
    }

    List<CartItemResponse> toItemResponseList(List<CartItem> cartItems);
    List<CartResponse> toResponseList(List<Cart> carts);

    @Mapping(source = "business.name", target = "businessName")
    @Mapping(target = "totalItems", expression = "java(cart.getTotalItems())")
    @Mapping(target = "subtotalBeforeDiscount", expression = "java(calculateSubtotalBeforeDiscount(cart))")
    @Mapping(target = "subtotal", expression = "java(cart.getSubtotal())")
    @Mapping(target = "totalDiscount", expression = "java(cart.getTotalDiscount())")
    @Mapping(target = "finalTotal", expression = "java(cart.getSubtotal())")
    @Mapping(target = "unavailableItems", expression = "java(cart.getUnavailableItemsCount())")
    CartResponse toResponse(Cart cart);

    @AfterMapping
    default void setCartItems(@MappingTarget CartResponse response, Cart cart) {
        if (cart.getItems() != null) {
            response.setItems(toItemResponseList(cart.getItems()));
        }
    }

    default PaginationResponse<CartResponse> toPaginationResponse(Page<Cart> cartPage, PaginationMapper paginationMapper) {
return paginationMapper.toPaginationResponse(cartPage, this::toResponseList);
    }

    @Mapping(source = "business.name", target = "businessName")
    @Mapping(target = "totalItems", expression = "java(cart.getTotalItems())")
    @Mapping(target = "subtotalBeforeDiscount", expression = "java(calculateSubtotalBeforeDiscount(cart))")
    @Mapping(target = "subtotal", expression = "java(cart.getSubtotal())")
    @Mapping(target = "totalDiscount", expression = "java(cart.getTotalDiscount())")
    @Mapping(target = "finalTotal", expression = "java(cart.getSubtotal())")
    CartSummaryResponse toSummaryResponse(Cart cart);

    @AfterMapping
    default void setSummaryCartItems(@MappingTarget CartSummaryResponse response, Cart cart) {
        if (cart.getItems() != null) {
            response.setItems(toItemResponseList(cart.getItems()));
        }
    }

    /**
     * Calculate cart subtotal before any discounts by summing items at original price (currentPrice * quantity)
     */
    default BigDecimal calculateSubtotalBeforeDiscount(Cart cart) {
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            return BigDecimal.ZERO;
        }

        return cart.getItems().stream()
                .map(item -> {
                    if (item.getCurrentPrice() != null && item.getQuantity() != null) {
                        return item.getCurrentPrice().multiply(new BigDecimal(item.getQuantity()));
                    }
                    return BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Create a new cart from helper DTO - pure MapStruct mapping
     */
    @Mapping(source = "userId", target = "userId")
    @Mapping(source = "businessId", target = "businessId")
    Cart createFromHelper(CartCreateHelper helper);
}
