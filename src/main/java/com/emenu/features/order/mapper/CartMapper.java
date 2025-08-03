package com.emenu.features.order.mapper;

import com.emenu.features.order.dto.request.CartItemRequest;
import com.emenu.features.order.dto.response.CartItemResponse;
import com.emenu.features.order.dto.response.CartResponse;
import com.emenu.features.order.models.Cart;
import com.emenu.features.order.models.CartItem;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class CartMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cartId", ignore = true)
    @Mapping(target = "cart", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "productSize", ignore = true)
    public abstract CartItem toEntity(CartItemRequest request);

    @Mapping(source = "product.name", target = "productName")
    @Mapping(target = "sizeName", expression = "java(cartItem.getSizeName())")
    @Mapping(target = "productImageUrl", expression = "java(cartItem.getProduct().getMainImageUrl())")
    @Mapping(target = "currentPrice", expression = "java(cartItem.getCurrentPrice())")
    @Mapping(target = "finalPrice", expression = "java(cartItem.getFinalPrice())")
    @Mapping(target = "totalPrice", expression = "java(cartItem.getTotalPrice())")
    @Mapping(target = "hasPromotion", expression = "java(cartItem.hasDiscount())")
    @Mapping(target = "discountAmount", expression = "java(cartItem.getDiscountAmount())")
    @Mapping(target = "isAvailable", expression = "java(cartItem.isProductAvailable())")
    @Mapping(target = "isInStock", expression = "java(cartItem.isProductInStock())")
    @Mapping(target = "unavailabilityReason", expression = "java(cartItem.getUnavailabilityReason())")
    @Mapping(source = "createdAt", target = "addedAt")
    public abstract CartItemResponse toItemResponse(CartItem cartItem);

    @AfterMapping
    protected void setPromotionDetails(@MappingTarget CartItemResponse response, CartItem cartItem) {
        // Set promotion details from product or product size
        if (cartItem.getProductSize() != null && cartItem.getProductSize().isPromotionActive()) {
            response.setPromotionType(cartItem.getProductSize().getPromotionType() != null ? 
                cartItem.getProductSize().getPromotionType().name() : null);
            response.setPromotionValue(cartItem.getProductSize().getPromotionValue());
            response.setPromotionEndDate(cartItem.getProductSize().getPromotionToDate());
        } else if (cartItem.getProduct() != null && cartItem.getProduct().isPromotionActive()) {
            response.setPromotionType(cartItem.getProduct().getPromotionType() != null ? 
                cartItem.getProduct().getPromotionType().name() : null);
            response.setPromotionValue(cartItem.getProduct().getPromotionValue());
            response.setPromotionEndDate(cartItem.getProduct().getPromotionToDate());
        }
    }

    public abstract List<CartItemResponse> toItemResponseList(List<CartItem> cartItems);

    @Mapping(source = "business.name", target = "businessName")
    @Mapping(target = "totalItems", expression = "java(cart.getTotalItems())")
    @Mapping(target = "subtotal", expression = "java(cart.getSubtotal())")
    @Mapping(target = "totalDiscount", expression = "java(cart.getTotalDiscount())")
    @Mapping(target = "finalTotal", expression = "java(cart.getSubtotal())")
    @Mapping(target = "isEmpty", expression = "java(cart.isEmpty())")
    @Mapping(source = "updatedAt", target = "lastUpdated")
    public abstract CartResponse toResponse(Cart cart);

    @AfterMapping
    protected void setCartItems(@MappingTarget CartResponse response, Cart cart) {
        if (cart.getItems() != null) {
            response.setItems(toItemResponseList(cart.getItems()));
        }
    }
}