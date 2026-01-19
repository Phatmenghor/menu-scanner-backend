package com.emenu.features.order.mapper;

import com.emenu.features.order.dto.request.CartItemRequest;
import com.emenu.features.order.dto.response.CartItemResponse;
import com.emenu.features.order.dto.response.CartResponse;
import com.emenu.features.order.models.Cart;
import com.emenu.features.order.models.CartItem;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", uses = {PaginationMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CartMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cartId", ignore = true)
    @Mapping(target = "cart", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "productSize", ignore = true)
    CartItem toEntity(CartItemRequest request);

    @Mapping(source = "product.name", target = "productName")
    @Mapping(target = "sizeName", expression = "java(cartItem.getSizeName())")
    @Mapping(target = "productImageUrl", expression = "java(cartItem.getProduct().getMainImageUrl())")
    @Mapping(target = "currentPrice", expression = "java(cartItem.getCurrentPrice())")
    @Mapping(target = "finalPrice", expression = "java(cartItem.getFinalPrice())")
    @Mapping(target = "totalPrice", expression = "java(cartItem.getTotalPrice())")
    @Mapping(target = "hasPromotion", expression = "java(cartItem.hasDiscount())")
    @Mapping(target = "isAvailable", expression = "java(cartItem.isAvailable())")
    CartItemResponse toItemResponse(CartItem cartItem);

    @AfterMapping
    default void setPromotionDetails(@MappingTarget CartItemResponse response, CartItem cartItem) {
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

    List<CartItemResponse> toItemResponseList(List<CartItem> cartItems);
    List<CartResponse> toResponseList(List<Cart> carts);

    @Mapping(source = "business.name", target = "businessName")
    @Mapping(target = "totalItems", expression = "java(cart.getTotalItems())")
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
}
