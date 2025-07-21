package com.emenu.features.notification.dto.response;

import lombok.Data;

import java.util.UUID;

@Data
public class MessageAttachmentResponse {
    
    private UUID id;
    private UUID messageId;
    private String fileName;
    private String filePath;
    private Long fileSize;
    private String fileType;
    private String contentType;
    private String downloadUrl;
}