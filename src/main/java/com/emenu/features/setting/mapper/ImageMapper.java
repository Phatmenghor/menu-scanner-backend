package com.emenu.features.setting.mapper;

import com.emenu.features.setting.dto.response.ImageDto;
import com.emenu.features.setting.dto.response.ImageResponse;
import com.emenu.features.setting.dto.request.ImageUploadRequest;
import com.emenu.features.setting.models.ImageEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.Base64;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ImageMapper {

    ImageMapper INSTANCE = Mappers.getMapper(ImageMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(source = "base64", target = "data")
    ImageEntity toEntity(ImageUploadRequest request);

    // Custom implementation for toDto to include imageUrl
    default ImageDto toDto(ImageEntity image) {
        if (image == null) {
            return null;
        }

        ImageDto imageDto = new ImageDto();
        imageDto.setId(image.getId());
        imageDto.setType(image.getType());
        imageDto.setImageUrl("/api/images/" + image.getId());

        return imageDto;
    }

    default ImageResponse toResponse(ImageEntity image) {
        if (image == null) {
            return null;
        }

        String base64Data = image.getData();
        // Remove prefix if it exists (like "data:image/png;base64,")
        if (base64Data.contains(",")) {
            base64Data = base64Data.substring(base64Data.indexOf(",") + 1);
        }

        return ImageResponse.builder()
                .id(image.getId())
                .type(image.getType())
                .data(Base64.getDecoder().decode(base64Data))
                .build();
    }
}
