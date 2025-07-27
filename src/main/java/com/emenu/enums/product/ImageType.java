package com.emenu.enums.product;

import lombok.Getter;

@Getter
public enum ImageType {
    MAIN("Main Image - Used for listing and primary display"),
    GALLERY("Gallery Image - Additional product images");

    private final String description;

    ImageType(String description) {
        this.description = description;
    }

    public boolean isMain() {
        return this == MAIN;
    }

    public boolean isGallery() {
        return this == GALLERY;
    }
}