package com.emenu.features.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Clean DTO for order delivery address snapshot - no inheritance, no nulls
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDeliveryAddressDto {
    private String street;
    private String city;
    private String district;
    private String commune;
    private String postalCode;
    private String country;
}
