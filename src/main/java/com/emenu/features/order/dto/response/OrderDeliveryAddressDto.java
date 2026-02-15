package com.emenu.features.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Clean DTO for order delivery address snapshot - mirrors DeliveryAddressRequest exactly
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDeliveryAddressDto {
    private String village;
    private String commune;
    private String district;
    private String province;
    private String streetNumber;
    private String houseNumber;
    private String note;
    private BigDecimal latitude;
    private BigDecimal longitude;
}
