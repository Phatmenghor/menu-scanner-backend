package com.emenu.features.business.dto.filter;

import com.emenu.enums.common.Status;
import com.emenu.shared.dto.BaseAllFilterRequest;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class BrandPublicFilterRequest extends BaseAllFilterRequest implements BrandFilterBase{
    @NotNull(message = "Business ID cannot be null")
    private UUID businessId;
    private Status status;
}