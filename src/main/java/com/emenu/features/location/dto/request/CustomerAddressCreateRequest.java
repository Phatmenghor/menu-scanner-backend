package com.emenu.features.location.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CustomerAddressCreateRequest {
    
    private String village; // Ex: Phum Svay Dangkum
    private String commune; // Ex: Sangkat Svay Dangkum
    
    @NotBlank(message = "District is required")
    private String district; // Ex: Krong Siem Reap
    
    @NotBlank(message = "Province is required")
    private String province; // Ex: Siem Reap
    
    private String streetNumber; // Ex: Street 63 or "St. 271"
    private String houseNumber; // Ex: "House No. 12B"
    private String note; // Optional note: "Leave with security"
    
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    private BigDecimal latitude; // For Google Maps
    
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    private BigDecimal longitude;
    
    private Boolean isDefault = false;
}