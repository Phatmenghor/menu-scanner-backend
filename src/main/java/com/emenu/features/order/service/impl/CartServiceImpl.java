package com.emenu.features.order.service.impl;

import com.emenu.exception.custom.NotFoundException;
import com.emenu.exception.custom.ValidationException;
import com.emenu.features.auth.models.User;
import com.emenu.features.order.dto.request.CartItemRequest;
import com.emenu.features.order.dto.response.CartResponse;
import com.emenu.features.order.dto.update.CartUpdateRequest;
import com.emenu.features.order.mapper.CartMapper;
import com.emenu.features.order.models.Cart;
import com.emenu.features.order.models.CartItem;
import com.emenu.features.order.repository.CartItemRepository;
import com.emenu.features.order.repository.CartRepository;
import com.emenu.features.order.service.CartService;
import com.emenu.features.product.models.Product;
import com.emenu.features.product.models.ProductSize;
import com.emenu.features.product.repository.ProductRepository;
import com.emenu.features.product.repository.ProductSizeRepository;
import com.emenu.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final ProductSizeRepository productSizeRepository;
    private final CartMapper cartMapper;
    private final SecurityUtils securityUtils;

    @Override
    public CartResponse addToCart(CartItemRequest request) {
        log.info("Adding/Updating item in cart - Product: {}, Size: {}, Quantity: {}", 
                request.getProductId(), request.getProductSizeId(), request.getQuantity());

        User currentUser = securityUtils.getCurrentUser();
        
        // Validate product and get business ID
        UUID businessId = validateProductAndGetBusinessId(request.getProductId(), request.getProductSizeId());
        
        // Get or create cart for user and business
        Cart cart = getOrCreateCart(currentUser.getId(), businessId);
        
        // Check if item already exists in cart
        Optional<CartItem> existingItem = cartItemRepository.findByCartIdAndProductIdAndSizeId(
                cart.getId(), request.getProductId(), request.getProductSizeId());
        
        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            
            if (request.getQuantity() == 0) {
                // Remove item when quantity is 0
                cartItemRepository.delete(item);
                log.info("Removed cart item: {}", item.getId());
            } else {
                // Set exact quantity (not add to existing)
                item.setQuantity(request.getQuantity());
                cartItemRepository.save(item);
                log.info("Updated cart item quantity to: {}", item.getQuantity());
            }
        } else {
            // Only create new item if quantity > 0
            if (request.getQuantity() > 0) {
                CartItem newItem = new CartItem(
                        cart.getId(),
                        request.getProductId(),
                        request.getProductSizeId(),
                        request.getQuantity()
                );
                cartItemRepository.save(newItem);
                log.info("Added new item to cart with quantity: {}", newItem.getQuantity());
            } else {
                log.info("Skipping cart item creation - quantity is 0 and item doesn't exist");
            }
        }

        return getCartResponse(currentUser.getId(), businessId);
    }

    @Override
    public CartResponse updateCartItem(CartUpdateRequest request) {
        log.info("Updating cart item: {} to quantity: {}", request.getCartItemId(), request.getQuantity());

        User currentUser = securityUtils.getCurrentUser();
        
        CartItem cartItem = cartItemRepository.findByIdAndIsDeletedFalse(request.getCartItemId())
                .orElseThrow(() -> new NotFoundException("Cart item not found"));

        // Validate product is still available
        validateProductAvailability(cartItem.getProductId(), cartItem.getProductSizeId());

        if (request.getQuantity() == 0) {
            // HARD DELETE - Remove item from cart completely
            cartItemRepository.delete(cartItem);
            log.info("Hard deleted cart item: {}", request.getCartItemId());
        } else {
            // Update quantity
            cartItem.setQuantity(request.getQuantity());
            cartItemRepository.save(cartItem);
            log.info("Updated cart item quantity to: {}", request.getQuantity());
        }

        // Get business ID from cart item
        UUID businessId = cartItem.getCart().getBusinessId();
        return getCartResponse(currentUser.getId(), businessId);
    }

    @Override
    public CartResponse removeFromCart(UUID cartItemId) {
        log.info("Removing item from cart: {}", cartItemId);

        User currentUser = securityUtils.getCurrentUser();
        
        CartItem cartItem = cartItemRepository.findByIdAndIsDeletedFalse(cartItemId)
                .orElseThrow(() -> new NotFoundException("Cart item not found"));

        UUID businessId = cartItem.getCart().getBusinessId();
        
        // HARD DELETE - Remove item from cart completely
        cartItemRepository.delete(cartItem);
        
        log.info("Hard deleted cart item: {}", cartItemId);
        return getCartResponse(currentUser.getId(), businessId);
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart(UUID businessId) {
        User currentUser = securityUtils.getCurrentUser();
        return getCartResponse(currentUser.getId(), businessId);
    }

    @Override
    public CartResponse clearCart(UUID businessId) {
        log.info("Clearing cart for business: {}", businessId);

        User currentUser = securityUtils.getCurrentUser();
        
        Optional<Cart> cartOpt = cartRepository.findByUserIdAndBusinessIdAndIsDeletedFalse(
                currentUser.getId(), businessId);
        
        if (cartOpt.isPresent()) {
            Cart cart = cartOpt.get();
            
            // HARD DELETE all cart items
            cart.getItems().forEach(cartItemRepository::delete);
            
            log.info("Hard deleted all cart items for user: {} and business: {}", currentUser.getId(), businessId);
        }

        return getCartResponse(currentUser.getId(), businessId);
    }

    // Private helper methods remain the same but simplified...
    private UUID validateProductAndGetBusinessId(UUID productId, UUID productSizeId) {
        if (productSizeId != null) {
            // Product with size
            ProductSize productSize = productSizeRepository.findById(productSizeId)
                    .orElseThrow(() -> new NotFoundException("Product size not found"));
            
            Product product = productSize.getProduct();
            validateProductAvailability(product);
            
            return product.getBusinessId();
        } else {
            // Product without size
            Product product = productRepository.findByIdAndIsDeletedFalse(productId)
                    .orElseThrow(() -> new NotFoundException("Product not found"));
            
            validateProductAvailability(product);
            
            return product.getBusinessId();
        }
    }

    private void validateProductAvailability(UUID productId, UUID productSizeId) {
        if (productSizeId != null) {
            ProductSize productSize = productSizeRepository.findById(productSizeId)
                    .orElseThrow(() -> new ValidationException("Product size no longer available"));
            validateProductAvailability(productSize.getProduct());
        } else {
            Product product = productRepository.findByIdAndIsDeletedFalse(productId)
                    .orElseThrow(() -> new ValidationException("Product no longer available"));
            validateProductAvailability(product);
        }
    }

    private void validateProductAvailability(Product product) {
        if (product == null) {
            throw new ValidationException("Product not found");
        }
        if (product.getIsDeleted()) {
            throw new ValidationException("Product has been removed");
        }
        if (!product.isActive()) {
            throw new ValidationException("Product is no longer available");
        }
        // Note: OUT_OF_STOCK products can still be added to cart for future purchase
    }

    private Cart getOrCreateCart(UUID userId, UUID businessId) {
        Optional<Cart> existingCart = cartRepository.findByUserIdAndBusinessIdAndIsDeletedFalse(userId, businessId);
        
        if (existingCart.isPresent()) {
            return existingCart.get();
        }
        
        // Create new cart
        Cart newCart = new Cart();
        newCart.setUserId(userId);
        newCart.setBusinessId(businessId);
        return cartRepository.save(newCart);
    }

    private CartResponse getCartResponse(UUID userId, UUID businessId) {
        Optional<Cart> cartOpt = cartRepository.findByUserIdAndBusinessIdWithItems(userId, businessId);
        
        if (cartOpt.isPresent()) {
            Cart cart = cartOpt.get();
            
            // ✅ UPDATED: Filter out items using the single isAvailable() method
            cart.getItems().removeIf(item -> !item.isAvailable());
            
            return cartMapper.toResponse(cart);
        }
        
        // Return empty cart response
        CartResponse emptyCart = new CartResponse();
        emptyCart.setUserId(userId);
        emptyCart.setBusinessId(businessId);
        emptyCart.setTotalItems(0);
        emptyCart.setUnavailableItems(0);
        return emptyCart;
    }
}