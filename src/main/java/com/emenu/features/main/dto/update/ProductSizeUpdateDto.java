package com.emenu.features.main.dto.update;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ProductSizeUpdateDto {
    
    private UUID id;

    @NotBlank(message = "Size name is required")
    private String name;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", message = "Price must be non-negative")
    private BigDecimal price;
    
    private String promotionType;
    private BigDecimal promotionValue;
    private LocalDateTime promotionFromDate;
    private LocalDateTime promotionToDate;
    
    private Boolean toDelete = false;
    
    public boolean isExisting() {
        return id != null;
    }
    
    public boolean isNew() {
        return id == null;
    }
    
    public boolean shouldDelete() {
        return Boolean.TRUE.equals(toDelete);
    }
    
    public boolean hasPromotionData() {
        return promotionType != null && promotionValue != null;
    }
    
    public void clearPromotion() {
        this.promotionType = null;
        this.promotionValue = null;
        this.promotionFromDate = null;
        this.promotionToDate = null;
    }
}