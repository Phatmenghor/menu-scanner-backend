package com.emenu.features.notification.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SystemEventDto {
    private String title;
    private String message;
    private String severity; // INFO, WARNING, ERROR
    private LocalDateTime timestamp;
}