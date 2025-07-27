package com.emenu.features.subdomain.service.impl;

import com.emenu.exception.custom.NotFoundException;
import com.emenu.features.auth.models.Business;
import com.emenu.features.auth.repository.BusinessRepository;
import com.emenu.features.subdomain.dto.filter.SubdomainFilterRequest;
import com.emenu.features.subdomain.dto.request.SubdomainGenerateRequest;
import com.emenu.features.subdomain.dto.response.SubdomainCheckResponse;
import com.emenu.features.subdomain.dto.response.SubdomainGenerateResponse;
import com.emenu.features.subdomain.dto.response.SubdomainResponse;
import com.emenu.features.subdomain.dto.response.SubdomainSuggestion;
import com.emenu.features.subdomain.mapper.SubdomainMapper;
import com.emenu.features.subdomain.models.Subdomain;
import com.emenu.features.subdomain.repository.SubdomainRepository;
import com.emenu.features.subdomain.service.SubdomainService;
import com.emenu.features.subdomain.specification.SubdomainSpecification;
import com.emenu.features.subdomain.utils.SubdomainUtils;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.pagination.PaginationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SubdomainServiceImpl implements SubdomainService {

    private final SubdomainRepository subdomainRepository;
    private final BusinessRepository businessRepository;
    private final SubdomainMapper subdomainMapper;

    @Value("${app.subdomain.base-domain:menu.com}")
    private String baseDomain;

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<SubdomainResponse> getAllSubdomains(SubdomainFilterRequest filter) {
        Specification<Subdomain> spec = SubdomainSpecification.buildSpecification(filter);
        
        int pageNo = filter.getPageNo() != null && filter.getPageNo() > 0 ? filter.getPageNo() - 1 : 0;
        Pageable pageable = PaginationUtils.createPageable(
                pageNo, filter.getPageSize(), filter.getSortBy(), filter.getSortDirection()
        );

        Page<Subdomain> subdomainPage = subdomainRepository.findAll(spec, pageable);
        return subdomainMapper.toPaginationResponse(subdomainPage);
    }

    @Override
    @Transactional(readOnly = true)
    public SubdomainResponse getSubdomainById(UUID id) {
        Subdomain subdomain = subdomainRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Subdomain not found"));
        return subdomainMapper.toResponse(subdomain);
    }

    @Override
    @Transactional(readOnly = true)
    public SubdomainResponse getSubdomainByBusinessId(UUID businessId) {
        Subdomain subdomain = subdomainRepository.findByBusinessIdAndIsDeletedFalse(businessId)
                .orElseThrow(() -> new NotFoundException("No subdomain found for business"));
        return subdomainMapper.toResponse(subdomain);
    }

    @Override
    @Transactional(readOnly = true)
    public SubdomainCheckResponse checkSubdomainAccess(String subdomainName) {
        log.debug("Checking subdomain access for: {}", subdomainName);

        var subdomainOpt = subdomainRepository.findBySubdomainWithBusiness(subdomainName);

        if (subdomainOpt.isEmpty()) {
            log.debug("Subdomain not found: {}", subdomainName);
            return SubdomainCheckResponse.notFound(subdomainName);
        }

        Subdomain subdomain = subdomainOpt.get();
        Business business = subdomain.getBusiness();

        // Increment access count asynchronously
        try {
            subdomainRepository.incrementAccessCount(subdomain.getId(), LocalDateTime.now());
        } catch (Exception e) {
            log.warn("Failed to increment access count for subdomain: {}", subdomainName, e);
        }

        // Check if subdomain is suspended
        if (!subdomain.isAccessible()) {
            log.debug("Subdomain is suspended: {}", subdomainName);
            return SubdomainCheckResponse.suspended(subdomainName, business.getName(), business.getId());
        }

        // Check subscription status
        if (!business.hasActiveSubscription()) {
            log.debug("Business subscription expired for subdomain: {}", subdomainName);
            return SubdomainCheckResponse.subscriptionExpired(subdomainName, business.getName(), business.getId());
        }

        // All checks passed - domain is accessible
        log.debug("Subdomain access granted: {}", subdomainName);
        return SubdomainCheckResponse.accessible(subdomainName, business.getName(), business.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isSubdomainAvailable(String subdomainName) {
        if (subdomainName == null || subdomainName.trim().isEmpty()) {
            return false;
        }
        
        String cleanedSubdomain = subdomainName.toLowerCase().trim();
        
        // Check format
        if (!isValidSubdomainFormat(cleanedSubdomain)) {
            return false;
        }
        
        // Check if already taken
        return !subdomainRepository.existsBySubdomainAndIsDeletedFalse(cleanedSubdomain);
    }

    @Override
    public SubdomainResponse createSubdomainForBusiness(UUID businessId, String preferredSubdomain) {
        log.info("✅ Auto-creating subdomain for business: {} with preferred name: {}", businessId, preferredSubdomain);

        // Validate business exists
        Business business = businessRepository.findByIdAndIsDeletedFalse(businessId)
                .orElseThrow(() -> new NotFoundException("Business not found"));

        // Check if business already has a subdomain
        if (subdomainRepository.existsByBusinessIdAndIsDeletedFalse(businessId)) {
            log.debug("Business already has a subdomain, returning existing one");
            return getSubdomainByBusinessId(businessId);
        }

        // Generate available subdomain name
        String availableSubdomain = generateAvailableSubdomain(preferredSubdomain);

        // Create subdomain entity
        Subdomain subdomain = new Subdomain();
        subdomain.setSubdomain(availableSubdomain);
        subdomain.setBusinessId(businessId);
        subdomain.setStatus(com.emenu.enums.subdomain.SubdomainStatus.ACTIVE); // Always active
        subdomain.setAccessCount(0L);
        subdomain.setNotes("Auto-created during business registration");

        Subdomain savedSubdomain = subdomainRepository.save(subdomain);
        
        log.info("✅ Subdomain created successfully: {} for business: {}", 
                availableSubdomain, business.getName());

        return subdomainMapper.toResponse(savedSubdomain);
    }

    @Override
    @Transactional(readOnly = true)
    public SubdomainGenerateResponse generateSubdomainSuggestions(SubdomainGenerateRequest request) {
        log.info("🎯 Generating subdomain suggestions for business: {}", request.getBusinessName());

        String businessName = request.getBusinessName();
        int suggestionCount = request.getSuggestionCount() != null ? request.getSuggestionCount() : 5;

        // Ensure reasonable limits
        suggestionCount = Math.min(Math.max(suggestionCount, 1), 20);

        List<SubdomainSuggestion> suggestions = new ArrayList<>();

        // 1. Primary suggestion - direct from business name
        String primarySubdomain = generateSubdomainFromBusinessName(businessName);
        addSuggestionIfValid(suggestions, primarySubdomain, "direct", 1);

        // 2. Short versions
        List<String> shortVersions = generateShortVersions(businessName);
        for (int i = 0; i < Math.min(shortVersions.size(), 2); i++) {
            addSuggestionIfValid(suggestions, shortVersions.get(i), "short", 2);
        }

        // 3. Numbered variations of primary
        if (suggestions.size() < suggestionCount) {
            for (int i = 1; i <= 10 && suggestions.size() < suggestionCount; i++) {
                addSuggestionIfValid(suggestions, primarySubdomain + i, "numbered", 3);
            }
        }

        // 4. Alternative word combinations
        if (suggestions.size() < suggestionCount) {
            List<String> alternatives = generateAlternatives(businessName);
            for (String alt : alternatives) {
                if (suggestions.size() >= suggestionCount) break;
                addSuggestionIfValid(suggestions, alt, "alternative", 4);
            }
        }

        // 5. Fallback random suggestions if still need more
        if (suggestions.size() < suggestionCount) {
            for (int i = 100; i < 999 && suggestions.size() < suggestionCount; i += 37) {
                String fallback = cleanSubdomainName(businessName.split(" ")[0]) + i;
                addSuggestionIfValid(suggestions, fallback, "fallback", 5);
            }
        }

        // Sort by priority and limit results
        List<SubdomainSuggestion> finalSuggestions = suggestions.stream()
                .sorted((a, b) -> Integer.compare(a.getPriority(), b.getPriority()))
                .limit(suggestionCount)
                .collect(Collectors.toList());

        // Get primary suggestion (first available one)
        String primarySuggestion = finalSuggestions.stream()
                .filter(SubdomainSuggestion::getIsAvailable)
                .map(SubdomainSuggestion::getSubdomain)
                .findFirst()
                .orElse(primarySubdomain);

        long availableCount = finalSuggestions.stream()
                .filter(SubdomainSuggestion::getIsAvailable)
                .count();

        log.info("✅ Generated {} suggestions for '{}', {} available",
                finalSuggestions.size(), businessName, availableCount);

        return SubdomainGenerateResponse.builder()
                .businessName(businessName)
                .primarySuggestion(primarySuggestion)
                .suggestions(finalSuggestions)
                .totalSuggestions(finalSuggestions.size())
                .availableSuggestions((int) availableCount)
                .baseDomain(baseDomain)
                .build();
    }

    private String generateSubdomainFromBusinessName(String businessName) {
        if (businessName == null || businessName.trim().isEmpty()) {
            return "business";
        }

        return businessName.toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9\\s-]", "") // Remove special characters
                .replaceAll("\\s+", "-")          // Replace spaces with hyphens
                .replaceAll("-{2,}", "-")         // Replace multiple hyphens with single
                .replaceAll("^-+|-+$", "")        // Remove leading/trailing hyphens
                .substring(0, Math.min(businessName.length(), 30)); // Limit length
    }

    private List<String> generateShortVersions(String businessName) {
        List<String> shortVersions = new ArrayList<>();

        if (businessName == null || businessName.trim().isEmpty()) {
            return shortVersions;
        }

        String[] words = businessName.toLowerCase().split("\\s+");

        // Single word abbreviations
        if (words.length > 1) {
            // First letters of each word
            StringBuilder abbreviation = new StringBuilder();
            for (String word : words) {
                if (!word.isEmpty()) {
                    abbreviation.append(word.charAt(0));
                }
            }
            if (abbreviation.length() >= 3) {
                shortVersions.add(abbreviation.toString());
            }

            // First word only
            String firstWord = cleanSubdomainName(words[0]);
            if (firstWord.length() >= 3) {
                shortVersions.add(firstWord);
            }

            // Last word only
            String lastWord = cleanSubdomainName(words[words.length - 1]);
            if (lastWord.length() >= 3 && !lastWord.equals(firstWord)) {
                shortVersions.add(lastWord);
            }
        }

        return shortVersions;
    }

    private void addSuggestionIfValid(List<SubdomainSuggestion> suggestions, String subdomain, String method, int priority) {
        if (subdomain == null || subdomain.isEmpty()) return;

        // Check if already exists in suggestions
        boolean alreadyExists = suggestions.stream()
                .anyMatch(s -> s.getSubdomain().equals(subdomain));
        if (alreadyExists) return;

        // Check format validity
        if (!SubdomainUtils.isValidSubdomainFormat(subdomain)) return;

        // Check if reserved
        if (SubdomainUtils.isReservedSubdomain(subdomain)) return;

        boolean isAvailable = isSubdomainAvailable(subdomain);

        SubdomainSuggestion suggestion = SubdomainSuggestion.builder()
                .subdomain(subdomain)
                .fullDomain(subdomain + "." + baseDomain)
                .fullUrl("https://" + subdomain + "." + baseDomain)
                .isAvailable(isAvailable)
                .generationMethod(method)
                .priority(isAvailable ? priority : priority + 10) // Available ones get higher priority
                .build();

        suggestions.add(suggestion);
    }

    private List<String> generateAlternatives(String businessName) {
        List<String> alternatives = new ArrayList<>();

        String baseClean = cleanSubdomainName(businessName);

        // Add common business suffixes
        String[] suffixes = {"menu", "restaurant", "cafe", "kitchen", "food", "eat", "dine"};
        for (String suffix : suffixes) {
            if (baseClean.length() + suffix.length() <= 25) {
                alternatives.add(baseClean + suffix);
                alternatives.add(baseClean + "-" + suffix);
            }
        }

        // Add common prefixes
        String[] prefixes = {"my", "the", "best", "top"};
        for (String prefix : prefixes) {
            if (prefix.length() + baseClean.length() <= 25) {
                alternatives.add(prefix + baseClean);
                alternatives.add(prefix + "-" + baseClean);
            }
        }

        return alternatives;
    }


    // Private helper methods
    private boolean isValidSubdomainFormat(String subdomain) {
        if (subdomain == null || subdomain.length() < 3 || subdomain.length() > 63) {
            return false;
        }
        
        // Must start and end with alphanumeric, can contain hyphens in between
        return subdomain.matches("^[a-z0-9][a-z0-9-]*[a-z0-9]$") || subdomain.matches("^[a-z0-9]{3,}$");
    }

    private String cleanSubdomainName(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "business";
        }

        return input.toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9]", "")      // Remove all non-alphanumeric
                .substring(0, Math.min(input.length(), 20)); // Limit length
    }


    private String generateAvailableSubdomain(String preferredSubdomain) {
        if (preferredSubdomain == null || preferredSubdomain.trim().isEmpty()) {
            preferredSubdomain = "restaurant";
        }

        String baseSubdomain = cleanSubdomainName(preferredSubdomain);
        String candidateSubdomain = baseSubdomain;
        int counter = 1;

        while (!isSubdomainAvailable(candidateSubdomain)) {
            candidateSubdomain = baseSubdomain + counter;
            counter++;
            
            // Prevent infinite loop
            if (counter > 1000) {
                candidateSubdomain = "restaurant" + System.currentTimeMillis();
                break;
            }
        }

        return candidateSubdomain;
    }
}