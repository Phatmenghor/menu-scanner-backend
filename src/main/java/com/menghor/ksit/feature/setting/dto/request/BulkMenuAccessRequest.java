package com.menghor.ksit.feature.setting.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Bulk request to update user menu access
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkMenuAccessRequest {
    private Long userId;
    private List<String> menuKeys;
    private boolean hasAccess;
}