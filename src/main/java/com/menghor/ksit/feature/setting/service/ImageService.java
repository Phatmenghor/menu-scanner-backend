package com.menghor.ksit.feature.setting.service;

import com.menghor.ksit.feature.setting.dto.request.ImageUploadRequest;
import com.menghor.ksit.feature.setting.dto.response.ImageDto;
import com.menghor.ksit.feature.setting.dto.response.ImageResponse;

import java.util.UUID;


public interface ImageService {

    ImageDto uploadImage(ImageUploadRequest request);

    ImageResponse getImageById(UUID id);

    void deleteImage(UUID id);
}