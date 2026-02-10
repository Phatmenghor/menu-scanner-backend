package com.emenu.features.order.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DeliveryAddressRequest {
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
