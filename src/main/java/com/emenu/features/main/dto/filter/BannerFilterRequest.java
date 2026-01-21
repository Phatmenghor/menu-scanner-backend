package com.emenu.features.main.dto.filter;

import com.emenu.enums.common.Status;
import com.emenu.features.main.dto.filter.base.BannerFilterBase;
import com.emenu.shared.dto.BaseFilterRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class BannerFilterRequest extends BaseFilterRequest implements BannerFilterBase {
    private UUID businessId;
    private Status status;
}

