package com.emenu.features.main.dto.update;

import com.emenu.enums.common.Status;
import lombok.Data;

@Data
public class BannerUpdateRequest {
    private String imageUrl;
    private String linkUrl;
    private Status status;
}