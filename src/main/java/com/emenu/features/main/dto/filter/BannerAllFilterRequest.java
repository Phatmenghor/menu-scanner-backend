package com.emenu.features.main.dto.filter;

import com.emenu.enums.common.Status;
import com.emenu.features.main.dto.filter.base.BannerFilterBase;
import com.emenu.shared.dto.BaseAllFilterRequest;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class BannerAllFilterRequest extends BaseAllFilterRequest implements BannerFilterBase {
    @NotNull(message = "Business ID cannot be null")
    private UUID businessId;
    private Status status;
}