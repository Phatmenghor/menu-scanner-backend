
package com.emenu.features.product.mapper;

import com.emenu.enums.product.ProductStatus;
import com.emenu.features.product.dto.response.ProductListingResponse;
import com.emenu.features.product.repository.ProductFavoriteRepository;
import com.emenu.shared.dto.PaginationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Fast mapper for product listings using native query results
 * Converts Object[] from native queries to ProductListingResponse
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProductListingMapper {

    private final ProductFavoriteRepository productFavoriteRepository;

    /**
     * Convert native query result Object[] to ProductListingResponse
     * Array indices must match the SELECT order in repository native queries
     */
    public ProductListingResponse mapNativeQueryToListing(Object[] row) {
        try {
            return ProductListingResponse.builder()
                    // Basic product fields (0-9)
                    .id((UUID) row[0])
                    .businessId((UUID) row[1])
                    .categoryId((UUID) row[2])
                    .brandId((UUID) row[3])
                    .name((String) row[4])
                    .description((String) row[5])
                    .status(ProductStatus.valueOf((String) row[6]))
                    .price((BigDecimal) row[7])
                    .promotionType((String) row[8])
                    .promotionValue((BigDecimal) row[9])
                    
                    // Statistics and timestamps (10-13)
                    .viewCount(getLongValue(row[10]))
                    .favoriteCount(getLongValue(row[11]))
                    .createdAt(convertToLocalDateTime(row[12]))
                    .updatedAt(convertToLocalDateTime(row[13]))
                    
                    // Related entity names (14-16)
                    .businessName((String) row[14])
                    .categoryName((String) row[15])
                    .brandName((String) row[16])
                    
                    // Calculated fields from database (17-20)
                    .mainImageUrl((String) row[17])
                    .hasSizes((Boolean) row[18])
                    .displayPrice((BigDecimal) row[19])
                    .hasActivePromotion((Boolean) row[20])
                    
                    // Derived fields
                    .publicUrl("/products/" + row[0])
                    .isFavorited(false) // Set separately for authenticated users
                    .build();
                    
        } catch (Exception e) {
            log.error("Error mapping native query result to ProductListingResponse: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to map product listing result", e);
        }
    }

    /**
     * Convert list of native query results to ProductListingResponse list
     */
    public List<ProductListingResponse> mapNativeQueryToListings(List<Object[]> rows) {
        return rows.stream()
                .map(this::mapNativeQueryToListing)
                .collect(Collectors.toList());
    }

    /**
     * Enrich responses with favorite status for authenticated user
     */
    public List<ProductListingResponse> enrichWithFavoriteStatus(List<ProductListingResponse> responses, UUID userId) {
        if (responses.isEmpty() || userId == null) {
            return responses;
        }

        try {
            // Get all product IDs
            List<UUID> productIds = responses.stream()
                    .map(ProductListingResponse::getId)
                    .collect(Collectors.toList());

            // Batch query for favorites - optimized
            Set<UUID> favoriteProductIds = productFavoriteRepository.findAll().stream()
                    .filter(fav -> fav.getUserId().equals(userId) && 
                                 productIds.contains(fav.getProductId()) && 
                                 !fav.getIsDeleted())
                    .map(fav -> fav.getProductId())
                    .collect(Collectors.toSet());

            // Set favorite status
            responses.forEach(response -> 
                    response.setIsFavorited(favoriteProductIds.contains(response.getId())));

            return responses;
        } catch (Exception e) {
            log.warn("Error enriching with favorite status: {}", e.getMessage());
            // Return responses with default false favorite status
            responses.forEach(response -> response.setIsFavorited(false));
            return responses;
        }
    }

    /**
     * Create fast pagination response from native query results
     */
    public PaginationResponse<ProductListingResponse> createFastPaginationResponse(
            List<Object[]> rows, 
            long totalElements, 
            int pageNo, 
            int pageSize,
            UUID userId) {
        
        List<ProductListingResponse> content = mapNativeQueryToListings(rows);
        
        // Enrich with favorite status if user is authenticated
        if (userId != null) {
            content = enrichWithFavoriteStatus(content, userId);
        }
        
        int totalPages = (int) Math.ceil((double) totalElements / pageSize);
        boolean isFirst = pageNo == 0;
        boolean isLast = pageNo >= totalPages - 1;
        boolean hasNext = pageNo < totalPages - 1;
        boolean hasPrevious = pageNo > 0;

        return PaginationResponse.<ProductListingResponse>builder()
                .content(content)
                .pageNo(pageNo + 1) // Convert to 1-based page number
                .pageSize(pageSize)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .first(isFirst)
                .last(isLast)
                .hasNext(hasNext)
                .hasPrevious(hasPrevious)
                .build();
    }

    // ================================
    // UTILITY METHODS
    // ================================

    /**
     * Convert various timestamp types to LocalDateTime
     */
    private LocalDateTime convertToLocalDateTime(Object timestamp) {
        if (timestamp == null) {
            return null;
        }
        
        if (timestamp instanceof Timestamp) {
            return ((Timestamp) timestamp).toLocalDateTime();
        } else if (timestamp instanceof LocalDateTime) {
            return (LocalDateTime) timestamp;
        } else if (timestamp instanceof java.sql.Date) {
            return ((java.sql.Date) timestamp).toLocalDate().atStartOfDay();
        } else {
            log.warn("Unexpected timestamp type: {}", timestamp.getClass());
            return null;
        }
    }

    /**
     * Convert various number types to Long
     */
    private Long getLongValue(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return 0L;
    }

    /**
     * Performance logging helper
     */
    public void logPerformanceMetrics(String operation, long startTime, int resultCount) {
        long duration = System.currentTimeMillis() - startTime;
        log.info("Performance - {}: {}ms for {} results (avg: {}ms/item)", 
                operation, duration, resultCount, 
                resultCount > 0 ? duration / resultCount : 0);
    }
}