package com.emenu.features.auth.dto.request;

import com.emenu.enums.MessageType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class BusinessMessageRequest {
    
    @NotBlank(message = "Subject is required")
    private String subject;
    
    @NotBlank(message = "Content is required")
    private String content;
    
    private MessageType messageType = MessageType.BUSINESS_UPDATE;
    private String priority = "NORMAL";
    
    // Send to specific staff members
    private List<UUID> staffIds;
    
    // Or send to all staff
    private Boolean sendToAllStaff = false;
    
    // Or send to customers
    private List<UUID> customerIds;
    
    // Or send to all customers
    private Boolean sendToAllCustomers = false;
}