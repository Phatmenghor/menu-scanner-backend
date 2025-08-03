package com.emenu.features.customer.dto.response;

import com.emenu.shared.dto.BaseAuditResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class CustomerAddressResponse extends BaseAuditResponse {
    private UUID userId;
    private String village;
    private String commune;
    private String district;
    private String province;
    private String streetNumber;
    private String houseNumber;
    private String note;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Boolean isDefault;
    private String fullAddress;
    private Boolean hasCoordinates;
}