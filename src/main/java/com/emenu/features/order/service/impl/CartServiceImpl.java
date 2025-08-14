package com.emenu.features.order.service.impl;

import com.emenu.exception.custom.NotFoundException;
import com.emenu.exception.custom.ValidationException;
import com.emenu.features.auth.models.User;
import com.emenu.features.order.dto.filter.CartFilterRequest;
import com.emenu.features.order.dto.request.CartItemRequest;
import com.emenu.features.order.dto.response.CartResponse;
import com.emenu.features.order.dto.update.CartUpdateRequest;
import com.emenu.features.order.mapper.CartMapper;
import com.emenu.features.order.models.Cart;
import com.emenu.features.order.models.CartItem;
import com.emenu.features.order.repository.CartItemRepository;
import com.emenu.features.order.repository.CartRepository;
import com.emenu.features.order.service.CartService;
import com.emenu.features.order.specification.CartSpecification;
import com.emenu.features.product.models.Product;
import com.emenu.features.product.models.ProductSize;
import com.emenu.features.product.repository.ProductRepository;
import com.emenu.features.product.repository.ProductSizeRepository;
import com.emenu.security.SecurityUtils;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.pagination.PaginationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
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
        UUID userId = currentUser.getId();

        // Validate product and get business ID
        UUID businessId = validateProductAndGetBusinessId(request.getProductId(), request.getProductSizeId());

        // Get or create cart for specific user and business
        Cart cart = getOrCreateUserBusinessCart(userId, businessId);

        // Check if item already exists in cart for this user, business, and product
        Optional<CartItem> existingItem = cartItemRepository.findByCartIdAndProductIdAndSizeId(
                cart.getId(), request.getProductId(), request.getProductSizeId());

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();

            if (request.getQuantity() == 0) {
                // HARD DELETE - Remove item completely from database
                cartItemRepository.delete(item);
                log.info("Hard deleted cart item: {} for user: {} and business: {}", 
                        item.getId(), userId, businessId);
            } else {
                // Update quantity
                item.setQuantity(request.getQuantity());
                cartItemRepository.save(item);
                log.info("Updated cart item quantity to: {} for user: {} and business: {}", 
                        item.getQuantity(), userId, businessId);
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
                log.info("Added new item to cart with quantity: {} for user: {} and business: {}", 
                        newItem.getQuantity(), userId, businessId);
            } else {
                log.info("Skipping cart item creation - quantity is 0 and item doesn't exist");
            }
        }

        // Return cart scoped to current user and business
        return getCartResponseByUserAndBusiness(userId, businessId);
    }

    @Override
    public CartResponse updateCartItem(CartUpdateRequest request) {
        log.info("Updating cart item: {} to quantity: {}", request.getCartItemId(), request.getQuantity());

        User currentUser = securityUtils.getCurrentUser();
        UUID userId = currentUser.getId();

        CartItem cartItem = cartItemRepository.findByIdAndIsDeletedFalse(request.getCartItemId())
                .orElseThrow(() -> new NotFoundException("Cart item not found"));

        // Security check: Ensure cart item belongs to current user
        Cart itemCart = cartItem.getCart();
        if (!itemCart.getUserId().equals(userId)) {
            throw new ValidationException("Cart item does not belong to current user");
        }

        UUID businessId = itemCart.getBusinessId();

        // Validate product is still available
        validateProductAvailability(cartItem.getProductId(), cartItem.getProductSizeId());

        if (request.getQuantity() == 0) {
            // HARD DELETE - Remove item from database completely
            cartItemRepository.delete(cartItem);
            log.info("Hard deleted cart item: {} for user: {} and business: {}", 
                    request.getCartItemId(), userId, businessId);
        } else {
            // Update quantity
            cartItem.setQuantity(request.getQuantity());
            cartItemRepository.save(cartItem);
            log.info("Updated cart item quantity to: {} for user: {} and business: {}", 
                    request.getQuantity(), userId, businessId);
        }

        return getCartResponseByUserAndBusiness(userId, businessId);
    }

    @Override
    public CartResponse removeFromCart(UUID cartItemId) {
        log.info("Removing item from cart: {}", cartItemId);

        User currentUser = securityUtils.getCurrentUser();
        UUID userId = currentUser.getId();

        CartItem cartItem = cartItemRepository.findByIdAndIsDeletedFalse(cartItemId)
                .orElseThrow(() -> new NotFoundException("Cart item not found"));

        // Security check: Ensure cart item belongs to current user
        Cart itemCart = cartItem.getCart();
        if (!itemCart.getUserId().equals(userId)) {
            throw new ValidationException("Cart item does not belong to current user");
        }

        UUID businessId = itemCart.getBusinessId();

        // HARD DELETE - Remove item from database completely
        cartItemRepository.delete(cartItem);
        log.info("Hard deleted cart item: {} for user: {} and business: {}", 
                cartItemId, userId, businessId);

        return getCartResponseByUserAndBusiness(userId, businessId);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<CartResponse> getMyCarts(CartFilterRequest filter) {
        User currentUser = securityUtils.getCurrentUser();
        UUID userId = currentUser.getId();

        // Force filter to current user
        filter.setUserId(userId);

        // If business user, also filter by their business
        if (currentUser.isBusinessUser()) {
            filter.setBusinessId(currentUser.getBusinessId());
        }

        Specification<Cart> spec = CartSpecification.buildSpecification(filter);

        int pageNo = filter.getPageNo() != null && filter.getPageNo() > 0 ? filter.getPageNo() - 1 : 0;
        Pageable pageable = PaginationUtils.createPageable(
                pageNo, filter.getPageSize(), filter.getSortBy(), filter.getSortDirection()
        );

        Page<Cart> cartPage = cartRepository.findAll(spec, pageable);

        // Filter out unavailable items from each cart
        cartPage.getContent().forEach(this::filterUnavailableItems);

        return cartMapper.toPaginationResponse(cartPage);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getMyCartItemsCount(CartFilterRequest filter) {
        User currentUser = securityUtils.getCurrentUser();
        UUID userId = currentUser.getId();
        
        log.info("Getting cart items count for user: {} and business: {}", userId, filter.getBusinessId());

        try {
            // Always filter by current user ID
            Long count = cartRepository.countItemsByUserIdAndBusinessId(userId, filter.getBusinessId());
            log.info("Cart items count: {} for user: {} and business: {}", count, userId, filter.getBusinessId());
            return count != null ? count : 0L;
        } catch (Exception e) {
            log.error("Error counting cart items for user: {} and business: {}: {}", 
                    userId, filter.getBusinessId(), e.getMessage(), e);
            return 0L;
        }
    }

    // ===== PRIVATE HELPER METHODS =====

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

    private Cart getOrCreateUserBusinessCart(UUID userId, UUID businessId) {
        Optional<Cart> existingCart = cartRepository.findByUserIdAndBusinessIdAndIsDeletedFalse(userId, businessId);

        if (existingCart.isPresent()) {
            return existingCart.get();
        }

        // Create new cart for user and business
        Cart newCart = new Cart();
        newCart.setUserId(userId);
        newCart.setBusinessId(businessId);
        Cart savedCart = cartRepository.save(newCart);
        
        log.info("Created new cart for user: {} and business: {}", userId, businessId);
        return savedCart;
    }

    private CartResponse getCartResponseByUserAndBusiness(UUID userId, UUID businessId) {
        Optional<Cart> cartOpt = cartRepository.findByUserIdAndBusinessIdWithItems(userId, businessId);

        if (cartOpt.isPresent()) {
            Cart cart = cartOpt.get();

            // Filter out unavailable items when returning response
            filterUnavailableItems(cart);

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

    /**
     * Filter out unavailable items from cart for response
     * This only filters the in-memory cart items, doesn't delete from database
     */
    private void filterUnavailableItems(Cart cart) {
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            return;
        }

        // Create new list with only available items for response
        List<CartItem> availableItems = new ArrayList<>();
        
        for (CartItem item : cart.getItems()) {
            if (isCartItemAvailable(item)) {
                availableItems.add(item);
            }
        }
        
        // Set the filtered items back to cart
        cart.setItems(availableItems);
    }

    /**
     * Check if cart item is available (product is active and not deleted)
     */
    private boolean isCartItemAvailable(CartItem cartItem) {
        try {
            // Check product availability
            Product product = cartItem.getProduct();
            if (product == null) {
                // Load product if not loaded
                Optional<Product> productOpt = productRepository.findByIdAndIsDeletedFalse(cartItem.getProductId());
                if (productOpt.isEmpty()) {
                    return false;
                }
                product = productOpt.get();
            }
            
            // Check if product is deleted or inactive
            if (product.getIsDeleted() || !product.isActive()) {
                return false;
            }

            // If cart item has product size, check size availability too
            if (cartItem.getProductSizeId() != null) {
                ProductSize productSize = cartItem.getProductSize();
                if (productSize == null) {
                    // Load product size if not loaded
                    Optional<ProductSize> sizeOpt = productSizeRepository.findById(cartItem.getProductSizeId());
                    if (sizeOpt.isEmpty()) {
                        return false;
                    }
                    productSize = sizeOpt.get();
                }
                
                // Check if product size is deleted
                return !productSize.getIsDeleted();
            }

            return true;
        } catch (Exception e) {
            log.error("Error checking cart item availability for item {}: {}", cartItem.getId(), e.getMessage());
            return false; // If there's an error, consider item unavailable
        }
    }

    /**
     * Get cart by user and business - with security validation
     */
    public CartResponse getCartByUserAndBusiness(UUID userId, UUID businessId) {
        User currentUser = securityUtils.getCurrentUser();
        
        // Security check: Ensure user can only access their own cart
        if (!currentUser.getId().equals(userId)) {
            throw new ValidationException("Cannot access cart of another user");
        }

        log.info("Getting cart for user: {} and business: {}", userId, businessId);
        return getCartResponseByUserAndBusiness(userId, businessId);
    }

    /**
     * Clear cart for user and business (hard delete all items)
     */
    public CartResponse clearCart(UUID userId, UUID businessId) {
        User currentUser = securityUtils.getCurrentUser();
        
        // Security check: Ensure user can only clear their own cart
        if (!currentUser.getId().equals(userId)) {
            throw new ValidationException("Cannot clear cart of another user");
        }

        log.info("Clearing cart for user: {} and business: {}", userId, businessId);

        Optional<Cart> cartOpt = cartRepository.findByUserIdAndBusinessIdWithItems(userId, businessId);
        if (cartOpt.isPresent()) {
            Cart cart = cartOpt.get();
            if (cart.getItems() != null && !cart.getItems().isEmpty()) {
                // HARD DELETE - Remove all items from database
                cartItemRepository.deleteAll(cart.getItems());
                log.info("Hard deleted {} cart items for user: {} and business: {}", 
                        cart.getItems().size(), userId, businessId);
            }
        }

        return getCartResponseByUserAndBusiness(userId, businessId);
    }

    /**
     * Get all carts for a specific business (business owner view)
     */
    @Transactional(readOnly = true)
    public PaginationResponse<CartResponse> getBusinessCarts(UUID businessId, CartFilterRequest filter) {
        User currentUser = securityUtils.getCurrentUser();
        
        // Security check: Business user can only access their own business carts
        if (currentUser.isBusinessUser() && !currentUser.getBusinessId().equals(businessId)) {
            throw new ValidationException("Cannot access carts of another business");
        }

        log.info("Getting carts for business: {}", businessId);

        filter.setBusinessId(businessId);
        
        Specification<Cart> spec = CartSpecification.buildSpecification(filter);

        int pageNo = filter.getPageNo() != null && filter.getPageNo() > 0 ? filter.getPageNo() - 1 : 0;
        Pageable pageable = PaginationUtils.createPageable(
                pageNo, filter.getPageSize(), filter.getSortBy(), filter.getSortDirection()
        );

        Page<Cart> cartPage = cartRepository.findAll(spec, pageable);

        // Filter out unavailable items from each cart
        cartPage.getContent().forEach(cart -> {
            filterUnavailableItems(cart);
        });

        return cartMapper.toPaginationResponse(cartPage);
    }

    /**
     * Get cart statistics for user
     */
    @Transactional(readOnly = true)
    public CartStats getCartStats(UUID userId) {
        User currentUser = securityUtils.getCurrentUser();
        
        // Security check: User can only get their own stats
        if (!currentUser.getId().equals(userId)) {
            throw new ValidationException("Cannot access cart stats of another user");
        }

        try {
            // Count active cart items across all businesses for this user
            long totalItems = cartRepository.findAll().stream()
                    .filter(cart -> cart.getUserId().equals(userId) && !cart.getIsDeleted())
                    .mapToLong(cart -> cart.getItems() != null ? cart.getItems().size() : 0)
                    .sum();

            // Count unique businesses in user's carts
            long uniqueBusinesses = cartRepository.findAll().stream()
                    .filter(cart -> cart.getUserId().equals(userId) && !cart.getIsDeleted())
                    .filter(cart -> cart.getItems() != null && !cart.getItems().isEmpty())
                    .map(Cart::getBusinessId)
                    .distinct()
                    .count();

            return new CartStats(totalItems, uniqueBusinesses);
        } catch (Exception e) {
            log.error("Error getting cart stats for user {}: {}", userId, e.getMessage(), e);
            return new CartStats(0, 0);
        }
    }

    /**
     * Statistics holder class
     */
    public static class CartStats {
        public final long totalItems;
        public final long uniqueBusinesses;

        public CartStats(long totalItems, long uniqueBusinesses) {
            this.totalItems = totalItems;
            this.uniqueBusinesses = uniqueBusinesses;
        }

        @Override
        public String toString() {
            return String.format("CartStats{totalItems=%d, uniqueBusinesses=%d}", 
                    totalItems, uniqueBusinesses);
        }
    }
}