package com.emenu.features.subdomain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubdomainSuggestion {
    private String subdomain;
    private String fullDomain;
    private String fullUrl;
    private Boolean isAvailable;
    private String generationMethod; // "direct", "short", "numbered", "alternative"
    private Integer priority; // 1 = highest priority, 5 = lowest
}