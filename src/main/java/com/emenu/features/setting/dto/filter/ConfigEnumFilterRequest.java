package com.emenu.features.setting.dto.filter;

import com.emenu.shared.dto.BaseFilterRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
public class ConfigEnumFilterRequest extends BaseFilterRequest {
    private UUID businessId;
}