package com.emenu.features.setting.controller;

import com.emenu.features.setting.dto.response.ImageDto;
import com.emenu.features.setting.dto.response.ImageResponse;
import com.emenu.features.setting.dto.request.ImageUploadRequest;
import com.emenu.features.setting.service.ImageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
@Slf4j
public class ImageController {
    
    private final ImageService imageService;

    /**
     * Uploads a new image
     */
    @PostMapping
    public ResponseEntity<ImageDto> uploadImage(@Valid @RequestBody ImageUploadRequest request) {
        log.info("Uploading image");
        ImageDto uploadedImage = imageService.uploadImage(request);
        return new ResponseEntity<>(uploadedImage, HttpStatus.CREATED);
    }

    /**
     * Retrieves image data by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getImageData(@PathVariable UUID id) {
        log.info("Get image data: {}", id);
        ImageResponse imageResponse = imageService.getImageById(id);
        return ResponseEntity
                .ok()
                .contentType(MediaType.valueOf(imageResponse.getType()))
                .body(imageResponse.getData());
    }

    /**
     * Deletes an image by its ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteImage(@PathVariable UUID id) {
        log.info("Delete image: {}", id);
        imageService.deleteImage(id);
        return ResponseEntity.noContent().build();
    }
}