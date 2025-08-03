package com.emenu.features.product.mapper;

import com.emenu.features.product.dto.response.BusinessPromotionResetResponse;
import com.emenu.features.product.dto.response.ProductPromotionResetResponse;
import com.emenu.features.product.dto.response.SizePromotionResetResponse;
import com.emenu.features.product.models.Product;
import com.emenu.features.product.models.ProductSize;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.UUID;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PromotionResponseMapper {

    @Mapping(target = "timestamp", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "businessId", target = "businessId")
    ProductPromotionResetResponse toProductResetResponse(Product product, UUID businessId,
                                                         Boolean productHadPromotion,
                                                         Integer sizesWithPromotions,
                                                         Integer totalPromotionsReset,
                                                         String message);

    @Mapping(target = "timestamp", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "size.id", target = "sizeId")
    @Mapping(source = "size.name", target = "sizeName")
    SizePromotionResetResponse toSizeResetResponse(Product product, ProductSize size,
                                                   UUID businessId, Boolean hadPromotion,
                                                   String originalPromotionType, String message);


    @Mapping(target = "timestamp", expression = "java(java.time.LocalDateTime.now())")
    BusinessPromotionResetResponse toBusinessResetResponse(UUID businessId,
                                                           Integer productPromotionsReset,
                                                           Integer sizePromotionsReset,
                                                           Integer totalReset,
                                                           String message);

    // Helper methods for easier mapping
    default ProductPromotionResetResponse createProductResetResponse(Product product, UUID businessId,
                                                                    boolean productHadPromotion,
                                                                    int sizesWithPromotions) {
        int totalReset = (productHadPromotion ? 1 : 0) + sizesWithPromotions;
        String message = String.format("Reset all promotions for product '%s' (%d total)", 
                product.getName(), totalReset);
        
        return toProductResetResponse(product, businessId, productHadPromotion, 
                sizesWithPromotions, totalReset, message);
    }

    default SizePromotionResetResponse createSizeResetResponse(Product product, ProductSize size,
                                                              UUID businessId, boolean hadPromotion,
                                                              String originalPromotionType) {
        String message = String.format("Reset promotion for size '%s' of product '%s'", 
                size.getName(), product.getName());
        
        return toSizeResetResponse(product, size, businessId, hadPromotion, originalPromotionType, message);
    }

    default BusinessPromotionResetResponse createBusinessResetResponse(UUID businessId,
                                                                      int productPromotionsReset,
                                                                      int sizePromotionsReset) {
        int totalReset = productPromotionsReset + sizePromotionsReset;
        String message = String.format("Reset all %d promotions for business", totalReset);
        
        return toBusinessResetResponse(businessId, productPromotionsReset, 
                sizePromotionsReset, totalReset, message);
    }
}