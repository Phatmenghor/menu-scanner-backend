
package com.emenu.features.user_management.dto.filter;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class PlatformUserFilterRequest extends UserFilterRequest {
    private String department;
    private String position;
}
