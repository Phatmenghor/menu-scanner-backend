package com.menghor.ksit.feature.setting.controller;

import com.menghor.ksit.feature.setting.dto.request.ImageUploadRequest;
import com.menghor.ksit.feature.setting.dto.response.ImageDto;
import com.menghor.ksit.feature.setting.dto.response.ImageResponse;
import com.menghor.ksit.feature.setting.service.ImageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {
    
    private final ImageService imageService;
    
    @PostMapping
    public ResponseEntity<ImageDto> uploadImage(@Valid @RequestBody ImageUploadRequest request) {
        ImageDto uploadedImage = imageService.uploadImage(request);
        return new ResponseEntity<>(uploadedImage, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getImageData(@PathVariable UUID id) {
        ImageResponse imageResponse = imageService.getImageById(id);
        return ResponseEntity
                .ok()
                .contentType(MediaType.valueOf(imageResponse.getType()))
                .body(imageResponse.getData());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteImage(@PathVariable UUID id) {
        imageService.deleteImage(id);
        return ResponseEntity.noContent().build();
    }
}