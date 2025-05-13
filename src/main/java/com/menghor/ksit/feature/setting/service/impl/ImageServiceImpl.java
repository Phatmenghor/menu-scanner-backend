package com.menghor.ksit.feature.setting.service.impl;

import com.menghor.ksit.exceptoins.error.NotFoundException;
import com.menghor.ksit.feature.setting.dto.request.ImageUploadRequest;
import com.menghor.ksit.feature.setting.dto.response.ImageDto;
import com.menghor.ksit.feature.setting.dto.response.ImageResponse;
import com.menghor.ksit.feature.setting.mapper.ImageMapper;
import com.menghor.ksit.feature.setting.models.ImageEntity;
import com.menghor.ksit.feature.setting.repository.ImageRepository;
import com.menghor.ksit.feature.setting.service.ImageService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {
    
    private final ImageRepository imageRepository;
    private final ImageMapper imageMapper;

    @Override
    @Transactional
    public ImageDto uploadImage(ImageUploadRequest request) {
        // Normalize the type field - add "image/" prefix if it doesn't have it
        if (!request.getType().contains("/")) {
            request.setType("image/" + request.getType());
        }

        ImageEntity image = imageMapper.toEntity(request);
        ImageEntity savedImage = imageRepository.save(image);
        return imageMapper.toDto(savedImage);
    }
    
    @Override
    @Transactional()
    public ImageResponse getImageById(UUID id) {
        ImageEntity image = imageRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Image not found with id: " + id));
        return imageMapper.toResponse(image);
    }

    
    @Override
    @Transactional
    public void deleteImage(UUID id) {
        if (!imageRepository.existsById(id)) {
            throw new NotFoundException("Image not found with id: " + id);
        }
        imageRepository.deleteById(id);
    }
}