package com.emenu.features.subdomain.service.impl;

import com.emenu.exception.custom.NotFoundException;
import com.emenu.exception.custom.ValidationException;
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
import java.util.*;
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

        String cleanedSubdomain = cleanSubdomain(subdomainName);

        // Check format
        if (!SubdomainUtils.isValidSubdomainFormat(cleanedSubdomain)) {
            return false;
        }

        // Check if reserved
        if (SubdomainUtils.isReservedSubdomain(cleanedSubdomain)) {
            return false;
        }

        // Check if already taken
        return !subdomainRepository.existsBySubdomainAndIsDeletedFalse(cleanedSubdomain);
    }

    @Override
    public SubdomainResponse createSubdomainForBusiness(UUID businessId, String preferredSubdomain) {
        log.info("Creating subdomain for business: {} with subdomain: {}", businessId, preferredSubdomain);

        // ✅ FIXED: Validate business exists
        Business business = businessRepository.findByIdAndIsDeletedFalse(businessId)
                .orElseThrow(() -> new ValidationException("Business not found"));

        // ✅ FIXED: Check if business already has a subdomain
        if (subdomainRepository.existsByBusinessIdAndIsDeletedFalse(businessId)) {
            log.warn("Business already has a subdomain, returning existing one");
            return getSubdomainByBusinessId(businessId);
        }

        // ✅ FIXED: Clean and validate the subdomain format
        String cleanedSubdomain = cleanSubdomain(preferredSubdomain);

        // ✅ FIXED: Validate subdomain format
        if (!SubdomainUtils.isValidSubdomainFormat(cleanedSubdomain)) {
            throw new ValidationException(
                    String.format("Invalid subdomain format: '%s'. Subdomain must be 3-63 characters, contain only lowercase letters, numbers, and hyphens, and cannot start or end with hyphen.",
                            preferredSubdomain)
            );
        }

        // ✅ FIXED: Check if subdomain is reserved
        if (SubdomainUtils.isReservedSubdomain(cleanedSubdomain)) {
            throw new ValidationException(
                    String.format("Subdomain '%s' is reserved and cannot be used. Please choose a different subdomain.",
                            cleanedSubdomain)
            );
        }

        // ✅ FIXED: Check if subdomain is already taken - NO AUTO-GENERATION
        if (subdomainRepository.existsBySubdomainAndIsDeletedFalse(cleanedSubdomain)) {
            throw new ValidationException(
                    String.format("Subdomain '%s' is already taken. Please choose a different subdomain.",
                            cleanedSubdomain)
            );
        }

        // ✅ FIXED: Create subdomain entity
        Subdomain subdomain = new Subdomain();
        subdomain.setSubdomain(cleanedSubdomain);
        subdomain.setBusinessId(businessId);
        subdomain.setStatus(com.emenu.enums.subdomain.SubdomainStatus.ACTIVE);
        subdomain.setAccessCount(0L);
        subdomain.setNotes("Created during business registration");

        try {
            Subdomain savedSubdomain = subdomainRepository.save(subdomain);
            log.info("✅ Subdomain created successfully: {} for business: {}",
                    cleanedSubdomain, business.getName());
            return subdomainMapper.toResponse(savedSubdomain);
        } catch (Exception e) {
            log.error("Failed to save subdomain: {}", e.getMessage(), e);
            throw new ValidationException(
                    String.format("Failed to create subdomain '%s': %s", cleanedSubdomain, e.getMessage())
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public SubdomainGenerateResponse generateSubdomainSuggestions(SubdomainGenerateRequest request) {
        log.info("🎯 Generating dynamic subdomain suggestions for business: {}", request.getBusinessName());

        String businessName = request.getBusinessName();
        int suggestionCount = request.getSuggestionCount() != null ? request.getSuggestionCount() : 5;

        // Ensure reasonable limits
        suggestionCount = Math.min(Math.max(suggestionCount, 1), 20);

        List<SubdomainSuggestion> suggestions = new ArrayList<>();

        // ✅ ENHANCED: More dynamic generation strategy
        String primarySubdomain = generateSubdomainFromBusinessName(businessName);

        // 1. Primary suggestion (highest priority)
        addSuggestionIfValid(suggestions, primarySubdomain, "direct", 1);

        // 2. ✅ NEW: Creative short versions (even for single words)
        generateCreativeShortVersions(businessName, suggestions, 2);

        // 3. ✅ NEW: Word variations and combinations
        generateWordVariations(businessName, suggestions, 2);

        // 4. ✅ ENHANCED: Business-related alternatives (higher priority than numbers)
        generateBusinessAlternatives(businessName, suggestions, 3);

        // 5. ✅ NEW: Creative combinations
        generateCreativeCombinations(businessName, suggestions, 4);

        // 6. Only add numbered variations if we still need more suggestions
        if (suggestions.size() < suggestionCount) {
            generateNumberedVariations(primarySubdomain, suggestions, suggestionCount, 5);
        }

        // 7. ✅ NEW: Fallback creative suggestions
        if (suggestions.size() < suggestionCount) {
            generateFallbackSuggestions(businessName, suggestions, suggestionCount, 6);
        }

        // Sort by priority and limit results
        List<SubdomainSuggestion> finalSuggestions = suggestions.stream()
                .sorted((a, b) -> Integer.compare(a.getPriority(), b.getPriority()))
                .limit(suggestionCount)
                .collect(Collectors.toList());

        // Get primary suggestion (first available one)
        String primarySuggestionFinal = finalSuggestions.stream()
                .filter(SubdomainSuggestion::getIsAvailable)
                .map(SubdomainSuggestion::getSubdomain)
                .findFirst()
                .orElse(primarySubdomain);

        long availableCount = finalSuggestions.stream()
                .filter(SubdomainSuggestion::getIsAvailable)
                .count();

        log.info("✅ Generated {} dynamic suggestions for '{}', {} available",
                finalSuggestions.size(), businessName, availableCount);

        return SubdomainGenerateResponse.builder()
                .businessName(businessName)
                .primarySuggestion(primarySuggestionFinal)
                .suggestions(finalSuggestions)
                .totalSuggestions(finalSuggestions.size())
                .availableSuggestions((int) availableCount)
                .baseDomain(baseDomain)
                .build();
    }

    private String cleanSubdomain(String input) {
        if (input == null || input.trim().isEmpty()) {
            throw new ValidationException("Subdomain cannot be empty");
        }

        return input.toLowerCase().trim();
    }

    // ✅ NEW: Generate creative short versions even for single words
    private void generateCreativeShortVersions(String businessName, List<SubdomainSuggestion> suggestions, int priority) {
        String cleanName = cleanSubdomainName(businessName);

        // For single words, create creative abbreviations
        if (cleanName.length() > 6) {
            // First 3 + last 3 characters
            if (cleanName.length() >= 6) {
                String shortVersion = cleanName.substring(0, 3) + cleanName.substring(cleanName.length() - 3);
                addSuggestionIfValid(suggestions, shortVersion, "abbreviated", priority);
            }

            // First 4 characters + "co" (company)
            if (cleanName.length() >= 4) {
                addSuggestionIfValid(suggestions, cleanName.substring(0, 4) + "co", "abbreviated", priority);
            }

            // First 5 characters
            if (cleanName.length() >= 5) {
                addSuggestionIfValid(suggestions, cleanName.substring(0, 5), "short", priority);
            }
        }

        // Multi-word handling
        String[] words = businessName.toLowerCase().split("\\s+");
        if (words.length > 1) {
            // First letters of each word
            StringBuilder abbreviation = new StringBuilder();
            for (String word : words) {
                if (!word.isEmpty()) {
                    abbreviation.append(word.charAt(0));
                }
            }
            if (abbreviation.length() >= 3) {
                addSuggestionIfValid(suggestions, abbreviation.toString(), "initials", priority);
            }

            // First word only
            String firstWord = cleanSubdomainName(words[0]);
            if (firstWord.length() >= 3) {
                addSuggestionIfValid(suggestions, firstWord, "first-word", priority);
            }

            // Last word only
            String lastWord = cleanSubdomainName(words[words.length - 1]);
            if (lastWord.length() >= 3 && !lastWord.equals(firstWord)) {
                addSuggestionIfValid(suggestions, lastWord, "last-word", priority);
            }

            // Combine first + last word (if more than 2 words)
            if (words.length > 2) {
                String combined = firstWord + lastWord;
                if (combined.length() <= 20) {
                    addSuggestionIfValid(suggestions, combined, "word-combo", priority);
                }
            }
        }
    }

    // ✅ NEW: Generate word variations
    private void generateWordVariations(String businessName, List<SubdomainSuggestion> suggestions, int priority) {
        String cleanName = cleanSubdomainName(businessName);

        // Remove common words and use remaining
        String[] commonWords = {"the", "a", "an", "and", "or", "of", "in", "on", "at", "to", "for", "with", "by"};
        String[] words = businessName.toLowerCase().split("\\s+");

        List<String> meaningfulWords = Arrays.stream(words)
                .filter(word -> !Arrays.asList(commonWords).contains(word.toLowerCase()))
                .filter(word -> word.length() >= 3)
                .map(this::cleanSubdomainName)
                .collect(Collectors.toList());

        // Combine meaningful words
        if (meaningfulWords.size() >= 2) {
            String combined = String.join("", meaningfulWords);
            if (combined.length() <= 25) {
                addSuggestionIfValid(suggestions, combined, "meaningful-combo", priority);
            }
        }

        // Reverse word order
        if (meaningfulWords.size() >= 2) {
            Collections.reverse(meaningfulWords);
            String reversed = String.join("", meaningfulWords);
            if (reversed.length() <= 25) {
                addSuggestionIfValid(suggestions, reversed, "word-reverse", priority);
            }
        }
    }

    // ✅ ENHANCED: Better business alternatives
    private void generateBusinessAlternatives(String businessName, List<SubdomainSuggestion> suggestions, int priority) {
        String baseClean = cleanSubdomainName(businessName);

        // Smart suffix selection based on business name
        List<String> smartSuffixes = getSmartSuffixes(businessName);

        for (String suffix : smartSuffixes) {
            if (baseClean.length() + suffix.length() <= 20) {
                addSuggestionIfValid(suggestions, baseClean + suffix, "business-suffix", priority);
                if (baseClean.length() + suffix.length() + 1 <= 20) {
                    addSuggestionIfValid(suggestions, baseClean + "-" + suffix, "business-suffix-dash", priority);
                }
            }
        }

        // Smart prefix selection
        List<String> smartPrefixes = getSmartPrefixes(businessName);

        for (String prefix : smartPrefixes) {
            if (prefix.length() + baseClean.length() <= 20) {
                addSuggestionIfValid(suggestions, prefix + baseClean, "business-prefix", priority);
                if (prefix.length() + baseClean.length() + 1 <= 20) {
                    addSuggestionIfValid(suggestions, prefix + "-" + baseClean, "business-prefix-dash", priority);
                }
            }
        }
    }

    // ✅ NEW: Creative combinations
    private void generateCreativeCombinations(String businessName, List<SubdomainSuggestion> suggestions, int priority) {
        String cleanName = cleanSubdomainName(businessName);

        // Location-based (for Cambodia)
        String[] cambodiaTerms = {"kh", "cambodia", "phnompenh", "asia", "sea"};
        for (String term : cambodiaTerms) {
            if (cleanName.length() + term.length() <= 20) {
                addSuggestionIfValid(suggestions, cleanName + term, "location-combo", priority);
                addSuggestionIfValid(suggestions, term + cleanName, "location-prefix", priority);
            }
        }

        // Year-based
        String currentYear = String.valueOf(java.time.Year.now().getValue());
        String shortYear = currentYear.substring(2);
        if (cleanName.length() + shortYear.length() <= 20) {
            addSuggestionIfValid(suggestions, cleanName + shortYear, "year-combo", priority);
        }

        // Quality terms
        String[] qualityTerms = {"best", "top", "prime", "elite", "pro", "plus", "max"};
        for (String term : qualityTerms) {
            if (cleanName.length() + term.length() <= 20) {
                addSuggestionIfValid(suggestions, cleanName + term, "quality-suffix", priority);
                addSuggestionIfValid(suggestions, term + cleanName, "quality-prefix", priority);
            }
        }

        // Size/Scale terms
        String[] scaleTerms = {"mini", "mega", "super", "ultra", "micro", "grand"};
        for (String term : scaleTerms) {
            if (cleanName.length() + term.length() <= 20) {
                addSuggestionIfValid(suggestions, term + cleanName, "scale-prefix", priority);
            }
        }
    }

    // ✅ IMPROVED: Only generate numbered variations as fallback
    private void generateNumberedVariations(String baseSubdomain, List<SubdomainSuggestion> suggestions, int maxSuggestions, int priority) {
        // Only add a few numbered variations, not many
        int numbersToAdd = Math.min(3, maxSuggestions - suggestions.size());

        for (int i = 1; i <= numbersToAdd && suggestions.size() < maxSuggestions; i++) {
            addSuggestionIfValid(suggestions, baseSubdomain + i, "numbered", priority);
        }
    }

    // ✅ NEW: Creative fallback suggestions
    private void generateFallbackSuggestions(String businessName, List<SubdomainSuggestion> suggestions, int maxSuggestions, int priority) {
        String cleanName = cleanSubdomainName(businessName);

        // Random creative combinations
        String[] creativeSuffixes = {"hub", "spot", "zone", "place", "house", "corner", "point", "base", "lab", "works"};

        for (String suffix : creativeSuffixes) {
            if (suggestions.size() >= maxSuggestions) break;
            if (cleanName.length() + suffix.length() <= 20) {
                addSuggestionIfValid(suggestions, cleanName + suffix, "creative-fallback", priority);
            }
        }

        // If still need more, use random numbers
        if (suggestions.size() < maxSuggestions) {
            Random random = new Random();
            for (int i = 0; i < 5 && suggestions.size() < maxSuggestions; i++) {
                int randomNum = random.nextInt(99) + 1;
                addSuggestionIfValid(suggestions, cleanName + randomNum, "random-number", priority + 1);
            }
        }
    }

    // ✅ NEW: Smart suffix selection based on business type
    private List<String> getSmartSuffixes(String businessName) {
        String lowerName = businessName.toLowerCase();
        List<String> suffixes = new ArrayList<>();

        // Food/Restaurant related
        if (containsAny(lowerName, "restaurant", "food", "kitchen", "cook", "chef", "eat", "dining", "cafe", "bar")) {
            suffixes.addAll(Arrays.asList("eats", "bites", "taste", "flavor", "dish", "meal", "feast"));
        }

        // Service related
        if (containsAny(lowerName, "service", "shop", "store", "market", "business", "company")) {
            suffixes.addAll(Arrays.asList("shop", "store", "market", "hub", "center", "zone"));
        }

        // Default suffixes
        suffixes.addAll(Arrays.asList("co", "inc", "group", "team", "club", "pro", "plus"));

        return suffixes.stream().distinct().limit(5).collect(Collectors.toList());
    }

    // ✅ NEW: Smart prefix selection
    private List<String> getSmartPrefixes(String businessName) {
        String lowerName = businessName.toLowerCase();
        List<String> prefixes = new ArrayList<>();

        // Quality indicators
        if (containsAny(lowerName, "premium", "luxury", "high", "quality", "best")) {
            prefixes.addAll(Arrays.asList("elite", "prime", "top", "best"));
        }

        // Modern/Tech related
        if (containsAny(lowerName, "modern", "tech", "digital", "smart", "new")) {
            prefixes.addAll(Arrays.asList("smart", "digital", "modern", "new", "tech"));
        }

        // Default prefixes
        prefixes.addAll(Arrays.asList("my", "the", "go", "get", "try", "see", "find"));

        return prefixes.stream().distinct().limit(4).collect(Collectors.toList());
    }

    // Helper method to check if text contains any of the keywords
    private boolean containsAny(String text, String... keywords) {
        return Arrays.stream(keywords).anyMatch(text::contains);
    }

    // Existing helper methods...
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