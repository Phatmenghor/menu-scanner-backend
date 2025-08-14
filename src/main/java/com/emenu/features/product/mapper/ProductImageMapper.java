package com.emenu.features.product.mapper;

import com.emenu.enums.product.ImageType;
import com.emenu.features.product.dto.request.ProductImageRequest;
import com.emenu.features.product.dto.response.ProductImageResponse;
import com.emenu.features.product.models.ProductImage;
import org.mapstruct.*;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class ProductImageMapper {

    // ================================
    // ENTITY CREATION MAPPING
    // ================================

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "productId", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(source = "imageType", target = "imageType", qualifiedByName = "stringToImageType")
    public abstract ProductImage toEntity(ProductImageRequest request);

    // ================================
    // RESPONSE MAPPING
    // ================================

    @Mapping(source = "imageType", target = "imageType", qualifiedByName = "imageTypeToString")
    public abstract ProductImageResponse toResponse(ProductImage productImage);

    /**
     * Convert list of ProductImage entities to sorted response list
     * MAIN images first, then GALLERY images sorted by creation date (newest first)
     */
    public List<ProductImageResponse> toSortedResponseList(List<ProductImage> productImages) {
        if (productImages == null || productImages.isEmpty()) {
            return List.of();
        }

        return productImages.stream()
                .sorted(createImageComparator())
                .map(this::toResponse)
                .toList();
    }

    /**
     * Convert list without sorting (for backward compatibility)
     */
    public abstract List<ProductImageResponse> toResponseList(List<ProductImage> productImages);

    // ================================
    // IMAGE MANAGEMENT UTILITIES
    // ================================

    /**
     * Create smart image entities with MAIN/GALLERY logic
     */
    public List<ProductImage> createSmartImageEntities(List<ProductImageRequest> imageRequests, 
                                                      UUID productId) {
        if (imageRequests == null || imageRequests.isEmpty()) {
            return List.of();
        }

        boolean isSingleImage = imageRequests.size() == 1;
        boolean hasMainImageSet = false;

        List<ProductImage> images = new java.util.ArrayList<>();

        for (ProductImageRequest request : imageRequests) {
            ProductImage image = toEntity(request);
            image.setProductId(productId);

            // Smart MAIN/GALLERY assignment
            if (isSingleImage) {
                image.setImageType(ImageType.MAIN);
                hasMainImageSet = true;
            } else {
                if ("MAIN".equalsIgnoreCase(request.getImageType()) && !hasMainImageSet) {
                    image.setImageType(ImageType.MAIN);
                    hasMainImageSet = true;
                } else {
                    image.setImageType(ImageType.GALLERY);
                }
            }

            images.add(image);
        }

        // If no MAIN image was set and we have multiple images, set first as MAIN
        if (!isSingleImage && !hasMainImageSet && !images.isEmpty()) {
            images.get(0).setImageType(ImageType.MAIN);
        }

        return images;
    }

    /**
     * Update existing images with smart MAIN/GALLERY logic
     */
    public List<ProductImage> updateImageEntitiesWithSmartLogic(
            List<ProductImageRequest> imageRequests,
            List<ProductImage> existingImages,
            UUID productId) {
        
        if (imageRequests == null || imageRequests.isEmpty()) {
            return List.of();
        }

        boolean isSingleImage = imageRequests.size() == 1;
        boolean hasMainImageSet = false;

        List<ProductImage> updatedImages = new java.util.ArrayList<>();

        for (ProductImageRequest request : imageRequests) {
            ProductImage image;
            
            if (request.getId() != null) {
                // Find existing image
                image = existingImages.stream()
                        .filter(existing -> existing.getId().equals(request.getId()))
                        .findFirst()
                        .orElseGet(() -> {
                            ProductImage newImage = toEntity(request);
                            newImage.setProductId(productId);
                            return newImage;
                        });
                
                // Update fields
                image.setImageUrl(request.getImageUrl());
            } else {
                // Create new image
                image = toEntity(request);
                image.setProductId(productId);
            }

            // Smart MAIN/GALLERY assignment
            if (isSingleImage) {
                image.setImageType(ImageType.MAIN);
                hasMainImageSet = true;
            } else {
                if ("MAIN".equalsIgnoreCase(request.getImageType()) && !hasMainImageSet) {
                    image.setImageType(ImageType.MAIN);
                    hasMainImageSet = true;
                } else {
                    image.setImageType(ImageType.GALLERY);
                }
            }

            updatedImages.add(image);
        }

        // Ensure one MAIN image exists
        if (!isSingleImage && !hasMainImageSet && !updatedImages.isEmpty()) {
            updatedImages.get(0).setImageType(ImageType.MAIN);
        }

        return updatedImages;
    }

    /**
     * Validate and fix image types (ensure only one MAIN image)
     */
    public List<ProductImage> validateAndFixImageTypes(List<ProductImage> images) {
        if (images == null || images.isEmpty()) {
            return images;
        }

        List<ProductImage> mainImages = images.stream()
                .filter(img -> img.getImageType() == ImageType.MAIN)
                .toList();

        // If no MAIN image, set first as MAIN
        if (mainImages.isEmpty()) {
            images.get(0).setImageType(ImageType.MAIN);
        }
        // If multiple MAIN images, keep first and set others as GALLERY
        else if (mainImages.size() > 1) {
            for (int i = 1; i < mainImages.size(); i++) {
                mainImages.get(i).setImageType(ImageType.GALLERY);
            }
        }

        return images;
    }

    /**
     * Find main image URL from sorted list
     */
    public String extractMainImageUrl(List<ProductImage> images) {
        if (images == null || images.isEmpty()) {
            return null;
        }

        return images.stream()
                .filter(img -> img.getImageType() == ImageType.MAIN)
                .findFirst()
                .map(ProductImage::getImageUrl)
                .orElse(images.get(0).getImageUrl()); // Fallback to first image
    }

    /**
     * Get sorted images by type and creation date
     */
    public List<ProductImage> getSortedImages(List<ProductImage> images) {
        if (images == null || images.isEmpty()) {
            return List.of();
        }

        return images.stream()
                .sorted(createImageComparator())
                .toList();
    }

    // ================================
    // HELPER METHODS
    // ================================

    /**
     * Create comparator for image sorting
     * Priority: MAIN images first, then GALLERY by creation date (newest first)
     */
    private Comparator<ProductImage> createImageComparator() {
        return (img1, img2) -> {
            // MAIN images first
            if (img1.getImageType() == ImageType.MAIN && img2.getImageType() != ImageType.MAIN) {
                return -1;
            }
            if (img1.getImageType() != ImageType.MAIN && img2.getImageType() == ImageType.MAIN) {
                return 1;
            }
            
            // If both are same type, sort by creation date (newest first)
            if (img1.getCreatedAt() != null && img2.getCreatedAt() != null) {
                return img2.getCreatedAt().compareTo(img1.getCreatedAt());
            }
            
            // Fallback to ID comparison for consistency
            return img1.getId().compareTo(img2.getId());
        };
    }

    /**
     * Check if image list has MAIN image
     */
    public boolean hasMainImage(List<ProductImage> images) {
        if (images == null || images.isEmpty()) {
            return false;
        }
        
        return images.stream()
                .anyMatch(img -> img.getImageType() == ImageType.MAIN);
    }

    /**
     * Count images by type
     */
    public long countImagesByType(List<ProductImage> images, ImageType type) {
        if (images == null || images.isEmpty()) {
            return 0;
        }
        
        return images.stream()
                .filter(img -> img.getImageType() == type)
                .count();
    }

    // ================================
    // MAPSTRUCT QUALIFIERS
    // ================================

    @Named("stringToImageType")
    protected ImageType stringToImageType(String imageType) {
        if (imageType == null || imageType.trim().isEmpty()) {
            return ImageType.GALLERY;
        }
        try {
            return ImageType.valueOf(imageType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ImageType.GALLERY;
        }
    }

    @Named("imageTypeToString")
    protected String imageTypeToString(ImageType imageType) {
        return imageType != null ? imageType.name() : "GALLERY";
    }

    // ================================
    // BATCH OPERATIONS
    // ================================

    /**
     * Process image requests for creation with smart logic
     */
    public ImageCreationResult processImageCreation(List<ProductImageRequest> imageRequests, 
                                                   UUID productId) {
        List<ProductImage> createdImages = createSmartImageEntities(imageRequests, productId);
        String mainImageUrl = extractMainImageUrl(createdImages);
        boolean hasMainImage = hasMainImage(createdImages);
        
        return new ImageCreationResult(createdImages, mainImageUrl, hasMainImage);
    }

    /**
     * Process image requests for update with smart logic
     */
    public ImageUpdateResult processImageUpdate(List<ProductImageRequest> imageRequests,
                                               List<ProductImage> existingImages,
                                               UUID productId) {
        List<ProductImage> updatedImages = updateImageEntitiesWithSmartLogic(
                imageRequests, existingImages, productId);
        
        List<UUID> imagesToDelete = findImagesToDelete(imageRequests, existingImages);
        String mainImageUrl = extractMainImageUrl(updatedImages);
        boolean hasMainImage = hasMainImage(updatedImages);
        
        return new ImageUpdateResult(updatedImages, imagesToDelete, mainImageUrl, hasMainImage);
    }

    /**
     * Find images that should be deleted (existing but not in request)
     */
    private List<UUID> findImagesToDelete(List<ProductImageRequest> imageRequests,
                                         List<ProductImage> existingImages) {
        if (existingImages == null || existingImages.isEmpty()) {
            return List.of();
        }

        java.util.Set<UUID> requestedIds = imageRequests.stream()
                .map(ProductImageRequest::getId)
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());

        return existingImages.stream()
                .map(ProductImage::getId)
                .filter(id -> !requestedIds.contains(id))
                .toList();
    }

    // ================================
    // RESULT CLASSES
    // ================================

    public static class ImageCreationResult {
        public final List<ProductImage> images;
        public final String mainImageUrl;
        public final boolean hasMainImage;

        public ImageCreationResult(List<ProductImage> images, String mainImageUrl, boolean hasMainImage) {
            this.images = images;
            this.mainImageUrl = mainImageUrl;
            this.hasMainImage = hasMainImage;
        }
    }

    public static class ImageUpdateResult {
        public final List<ProductImage> images;
        public final List<UUID> imagesToDelete;
        public final String mainImageUrl;
        public final boolean hasMainImage;

        public ImageUpdateResult(List<ProductImage> images, List<UUID> imagesToDelete,
                               String mainImageUrl, boolean hasMainImage) {
            this.images = images;
            this.imagesToDelete = imagesToDelete;
            this.mainImageUrl = mainImageUrl;
            this.hasMainImage = hasMainImage;
        }
    }
}