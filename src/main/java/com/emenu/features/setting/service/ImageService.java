package com.emenu.features.setting.service;

import com.emenu.features.setting.dto.response.ImageDto;
import com.emenu.features.setting.dto.response.ImageResponse;
import com.emenu.features.setting.dto.request.ImageUploadRequest;

import java.util.UUID;


public interface ImageService {

    ImageDto uploadImage(ImageUploadRequest request);

    ImageResponse getImageById(UUID id);

    void deleteImage(UUID id);
}