package com.emenu.features.order.service.impl;

import com.emenu.exception.custom.NotFoundException;
import com.emenu.exception.custom.ValidationException;
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

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart(UUID userId, UUID businessId) {
        log.info("Getting cart for user: {} and business: {}", userId, businessId);
        
        Optional<Cart> cartOpt = cartRepository.findByUserIdAndBusinessIdWithItems(userId, businessId);
        
        if (cartOpt.isPresent()) {
            Cart cart = cartOpt.get();
            // Filter out unavailable items for display
            filterUnavailableItems(cart);
            return cartMapper.toResponse(cart);
        }
        
        // Return empty cart response
        return createEmptyCartResponse(userId, businessId);
    }

    @Override
    public CartResponse addToCart(UUID userId, UUID businessId, CartItemRequest request) {
        log.info("Adding item to cart - User: {}, Business: {}, Product: {}, Quantity: {}", 
                userId, businessId, request.getProductId(), request.getQuantity());

        // Validate product and get business ID from product
        UUID productBusinessId = validateProductAndGetBusinessId(request.getProductId(), request.getProductSizeId());
        
        // Ensure product belongs to the specified business
        if (!productBusinessId.equals(businessId)) {
            throw new ValidationException("Product does not belong to the specified business");
        }

        // Get or create cart
        Cart cart = getOrCreateCart(userId, businessId);

        // Check if item already exists in cart
        Optional<CartItem> existingItem = cartItemRepository.findByCartIdAndProductIdAndSizeId(
                cart.getId(), request.getProductId(), request.getProductSizeId());

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            
            if (request.getQuantity() == 0) {
                // Remove item completely
                cartItemRepository.delete(item);
                log.info("Removed cart item: {} for user: {}", item.getId(), userId);
            } else {
                // Update quantity
                item.setQuantity(request.getQuantity());
                cartItemRepository.save(item);
                log.info("Updated cart item quantity to: {} for user: {}", item.getQuantity(), userId);
            }
        } else {
            // Create new item only if quantity > 0
            if (request.getQuantity() > 0) {
                CartItem newItem = new CartItem(
                        cart.getId(),
                        request.getProductId(),
                        request.getProductSizeId(),
                        request.getQuantity()
                );
                cartItemRepository.save(newItem);
                log.info("Added new item to cart with quantity: {} for user: {}", newItem.getQuantity(), userId);
            }
        }

        return getCart(userId, businessId);
    }

    @Override
    public CartResponse updateCartItem(UUID userId, UUID businessId, CartUpdateRequest request) {
        log.info("Updating cart item: {} to quantity: {} for user: {}", 
                request.getCartItemId(), request.getQuantity(), userId);

        CartItem cartItem = cartItemRepository.findByIdAndIsDeletedFalse(request.getCartItemId())
                .orElseThrow(() -> new NotFoundException("Cart item not found"));

        // Security check: Ensure cart item belongs to current user
        Cart itemCart = cartItem.getCart();
        if (!itemCart.getUserId().equals(userId)) {
            throw new ValidationException("Cart item does not belong to current user");
        }

        // Validate product is still available
        validateProductAvailability(cartItem.getProductId(), cartItem.getProductSizeId());

        if (request.getQuantity() == 0) {
            // Remove item completely
            cartItemRepository.delete(cartItem);
            log.info("Removed cart item: {} for user: {}", request.getCartItemId(), userId);
        } else {
            // Update quantity
            cartItem.setQuantity(request.getQuantity());
            cartItemRepository.save(cartItem);
            log.info("Updated cart item quantity to: {} for user: {}", request.getQuantity(), userId);
        }

        return getCart(userId, businessId);
    }

    @Override
    public CartResponse removeFromCart(UUID userId, UUID businessId, UUID cartItemId) {
        log.info("Removing cart item: {} for user: {}", cartItemId, userId);

        CartItem cartItem = cartItemRepository.findByIdAndIsDeletedFalse(cartItemId)
                .orElseThrow(() -> new NotFoundException("Cart item not found"));

        // Security check: Ensure cart item belongs to current user
        Cart itemCart = cartItem.getCart();
        if (!itemCart.getUserId().equals(userId)) {
            throw new ValidationException("Cart item does not belong to current user");
        }

        // Remove item completely
        cartItemRepository.delete(cartItem);
        log.info("Removed cart item: {} for user: {}", cartItemId, userId);

        return getCart(userId, businessId);
    }

    @Override
    public CartResponse clearCart(UUID userId, UUID businessId) {
        log.info("Clearing cart for user: {} and business: {}", userId, businessId);

        Optional<Cart> cartOpt = cartRepository.findByUserIdAndBusinessIdWithItems(userId, businessId);
        if (cartOpt.isPresent()) {
            Cart cart = cartOpt.get();
            if (cart.getItems() != null && !cart.getItems().isEmpty()) {
                // Remove all items from database
                cartItemRepository.deleteAll(cart.getItems());
                log.info("Cleared {} cart items for user: {}", cart.getItems().size(), userId);
            }
        }

        return getCart(userId, businessId);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getCartItemsCount(UUID userId, UUID businessId) {
        log.info("Getting cart items count for user: {} and business: {}", userId, businessId);
        
        try {
            Long count = cartRepository.countItemsByUserIdAndBusinessId(userId, businessId);
            return count != null ? count : 0L;
        } catch (Exception e) {
            log.error("Error counting cart items for user: {}: {}", userId, e.getMessage());
            return 0L;
        }
    }

    // ===== PRIVATE HELPER METHODS =====

    private Cart getOrCreateCart(UUID userId, UUID businessId) {
        Optional<Cart> existingCart = cartRepository.findByUserIdAndBusinessIdAndIsDeletedFalse(userId, businessId);

        if (existingCart.isPresent()) {
            return existingCart.get();
        }

        // Create new cart
        Cart newCart = new Cart();
        newCart.setUserId(userId);
        newCart.setBusinessId(businessId);
        Cart savedCart = cartRepository.save(newCart);
        
        log.info("Created new cart for user: {} and business: {}", userId, businessId);
        return savedCart;
    }

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
    }

    private void filterUnavailableItems(Cart cart) {
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            return;
        }

        // Collect unavailable items to delete from database
        var unavailableItems = cart.getItems().stream()
                .filter(item -> !isCartItemAvailable(item))
                .toList();

        if (!unavailableItems.isEmpty()) {
            // Hard delete unavailable items from database
            cartItemRepository.deleteAll(unavailableItems);
            log.info("Deleted {} unavailable cart items from database for cart: {}",
                    unavailableItems.size(), cart.getId());
        }

        // Filter items to only include available ones for response
        cart.getItems().removeIf(item -> !isCartItemAvailable(item));
    }

    private boolean isCartItemAvailable(CartItem cartItem) {
        try {
            Product product = cartItem.getProduct();
            if (product == null) {
                Optional<Product> productOpt = productRepository.findByIdAndIsDeletedFalse(cartItem.getProductId());
                if (productOpt.isEmpty()) {
                    return false;
                }
                product = productOpt.get();
            }
            
            if (product.getIsDeleted() || !product.isActive()) {
                return false;
            }

            if (cartItem.getProductSizeId() != null) {
                ProductSize productSize = cartItem.getProductSize();
                if (productSize == null) {
                    Optional<ProductSize> sizeOpt = productSizeRepository.findById(cartItem.getProductSizeId());
                    if (sizeOpt.isEmpty()) {
                        return false;
                    }
                    productSize = sizeOpt.get();
                }
                return !productSize.getIsDeleted();
            }

            return true;
        } catch (Exception e) {
            log.error("Error checking cart item availability for item {}: {}", cartItem.getId(), e.getMessage());
            return false;
        }
    }

    private CartResponse createEmptyCartResponse(UUID userId, UUID businessId) {
        CartResponse emptyCart = new CartResponse();
        emptyCart.setUserId(userId);
        emptyCart.setBusinessId(businessId);
        emptyCart.setTotalItems(0);
        emptyCart.setUnavailableItems(0);
        return emptyCart;
    }
}