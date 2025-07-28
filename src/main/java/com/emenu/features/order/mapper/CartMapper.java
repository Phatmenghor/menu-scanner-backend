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
    @Mapping(target = "unitPrice", ignore = true) // Will be set from product/size
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    public abstract CartItem toEntity(CartItemRequest request);

    @Mapping(source = "product.name", target = "productName")
    @Mapping(target = "sizeName", expression = "java(cartItem.getSizeName())")
    @Mapping(target = "productImageUrl", expression = "java(cartItem.getProduct().getMainImageUrl())")
    @Mapping(target = "finalPrice", expression = "java(cartItem.getFinalPrice())")
    @Mapping(target = "totalPrice", expression = "java(cartItem.getTotalPrice())")
    @Mapping(target = "hasPromotion", expression = "java(cartItem.hasDiscount())")
    @Mapping(source = "createdAt", target = "addedAt")
    public abstract CartItemResponse toItemResponse(CartItem cartItem);

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