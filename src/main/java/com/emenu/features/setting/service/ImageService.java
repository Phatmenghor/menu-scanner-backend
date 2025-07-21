package com.emenu.features.setting.service;

import com.emenu.features.setting.dto.ImageDto;
import com.emenu.features.setting.dto.ImageResponse;
import com.emenu.features.setting.dto.ImageUploadRequest;

import java.util.UUID;


public interface ImageService {

    ImageDto uploadImage(ImageUploadRequest request);

    ImageResponse getImageById(UUID id);

    void deleteImage(UUID id);
}