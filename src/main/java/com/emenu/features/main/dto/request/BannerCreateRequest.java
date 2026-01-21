package com.emenu.features.main.dto.request;

import com.emenu.enums.common.Status;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BannerCreateRequest {
    
    @NotBlank(message = "Image URL is required")
    private String imageUrl;
    
    private String linkUrl;
    private Status status = Status.ACTIVE;
}
