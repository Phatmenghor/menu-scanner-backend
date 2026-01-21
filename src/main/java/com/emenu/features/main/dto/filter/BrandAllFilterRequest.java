package com.emenu.features.main.dto.filter;

import com.emenu.enums.common.Status;
import com.emenu.features.main.dto.filter.base.BrandFilterBase;
import com.emenu.shared.dto.BaseAllFilterRequest;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class BrandAllFilterRequest extends BaseAllFilterRequest implements BrandFilterBase {
    @NotNull(message = "Business ID cannot be null")
    private UUID businessId;
    private Status status;
}