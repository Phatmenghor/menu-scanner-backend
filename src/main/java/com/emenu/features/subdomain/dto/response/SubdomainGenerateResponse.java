package com.emenu.features.subdomain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubdomainGenerateResponse {
    private String businessName;
    private String primarySuggestion; // The best available option
    private List<SubdomainSuggestion> suggestions;
    private Integer totalSuggestions;
    private Integer availableSuggestions;
    private String baseDomain; // "menu.com"
}