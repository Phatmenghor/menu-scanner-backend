package com.emenu.features.order.service.impl;

import com.emenu.exception.custom.NotFoundException;
import com.emenu.exception.custom.ValidationException;
import com.emenu.features.auth.models.Business;
import com.emenu.features.auth.models.User;
import com.emenu.features.auth.repository.BusinessRepository;
import com.emenu.features.order.dto.filter.BusinessExchangeRateFilterRequest;
import com.emenu.features.order.dto.request.BusinessExchangeRateCreateRequest;
import com.emenu.features.order.dto.response.BusinessExchangeRateResponse;
import com.emenu.features.order.dto.update.BusinessExchangeRateUpdateRequest;
import com.emenu.features.order.mapper.BusinessExchangeRateMapper;
import com.emenu.features.order.models.BusinessExchangeRate;
import com.emenu.features.order.repository.BusinessExchangeRateRepository;
import com.emenu.features.order.service.BusinessExchangeRateService;
import com.emenu.security.SecurityUtils;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.pagination.PaginationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BusinessExchangeRateServiceImpl implements BusinessExchangeRateService {

    private final BusinessExchangeRateRepository exchangeRateRepository;
    private final BusinessRepository businessRepository;
    private final BusinessExchangeRateMapper exchangeRateMapper;
    private final SecurityUtils securityUtils;
    private final com.emenu.shared.mapper.PaginationMapper paginationMapper;

    @Override
    public BusinessExchangeRateResponse createBusinessExchangeRate(BusinessExchangeRateCreateRequest request) {
        log.info("Creating business exchange rate for business: {} - Rate: {}", request.getBusinessId(), request.getUsdToKhrRate());

        UUID businessId = determineBusinessId(request.getBusinessId());

        // Validate busin¶ess exists
        Business business = businessRepository.findByIdAndIsDeletedFalse(businessId)
                .orElseThrow(() -> new NotFoundException("Business not found"));

        // Deactivate existing active rate for this business
        deactivateCurrentActiveRate(businessId);

        BusinessExchangeRate exchangeRate = exchangeRateMapper.toEntity(request);
        exchangeRate.setIsActive(true); // New rate is always active

        BusinessExchangeRate savedExchangeRate = exchangeRateRepository.save(exchangeRate);

        log.info("Business exchange rate created successfully for {}: {} KHR per USD",
                business.getName(), savedExchangeRate.getUsdToKhrRate());

        return exchangeRateMapper.toResponse(savedExchangeRate);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<BusinessExchangeRateResponse> getAllBusinessExchangeRates(BusinessExchangeRateFilterRequest filter) {
        log.info("Fetching all business exchange rates with filters");

        UUID businessId = determineBusinessId(filter.getBusinessId());
        filter.setBusinessId(businessId);

        Pageable pageable = PaginationUtils.createPageable(
                filter.getPageNo(), filter.getPageSize(), filter.getSortBy(), filter.getSortDirection()
        );

        Page<BusinessExchangeRate> page = exchangeRateRepository.findAllWithFilters(
                businessId,
                filter.getIsActive(),
                filter.getSearch(),
                pageable
        );
        return exchangeRateMapper.toPaginationResponse(page, paginationMapper);
    }

    @Override
    @Transactional(readOnly = true)
    public BusinessExchangeRateResponse getBusinessExchangeRateById(UUID id) {
        log.info("Fetching business exchange rate by ID: {}", id);

        BusinessExchangeRate exchangeRate = findExchangeRateById(id);
        return exchangeRateMapper.toResponse(exchangeRate);
    }

    @Override
    public BusinessExchangeRateResponse updateBusinessExchangeRate(UUID id, BusinessExchangeRateUpdateRequest request) {
        log.info("Updating business exchange rate: {}", id);

        BusinessExchangeRate exchangeRate = findExchangeRateById(id);

        exchangeRateMapper.updateEntity(request, exchangeRate);
        BusinessExchangeRate updatedExchangeRate = exchangeRateRepository.save(exchangeRate);

        log.info("Business exchange rate updated successfully: {} - New rate: {}",
                id, updatedExchangeRate.getUsdToKhrRate());

        return exchangeRateMapper.toResponse(updatedExchangeRate);
    }

    @Override
    public BusinessExchangeRateResponse deleteBusinessExchangeRate(UUID id) {
        log.info("Deleting business exchange rate: {}", id);

        BusinessExchangeRate exchangeRate = findExchangeRateById(id);

        // Don't allow deletion of the only active rate for a business
        if (exchangeRate.getIsActive() && exchangeRateRepository.countActiveRates(exchangeRate.getBusinessId()) == 1) {
            throw new ValidationException("Cannot delete the only active exchange rate. Create a new rate first.");
        }

        exchangeRate.softDelete();
        exchangeRate = exchangeRateRepository.save(exchangeRate);

        log.info("Business exchange rate deleted successfully: {}", id);
        return exchangeRateMapper.toResponse(exchangeRate);
    }

    @Override
    @Transactional(readOnly = true)
    public BusinessExchangeRateResponse getActiveRateByBusinessId(UUID businessId) {
        log.info("Fetching active exchange rate for business: {}", businessId);

        BusinessExchangeRate activeRate = exchangeRateRepository.findActiveRateByBusinessId(businessId)
                .orElseThrow(() -> new NotFoundException("No active exchange rate found for business"));

        return exchangeRateMapper.toResponse(activeRate);
    }

    // Private helper methods

    private BusinessExchangeRate findExchangeRateById(UUID id) {
        return exchangeRateRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Business exchange rate not found"));
    }

    private UUID determineBusinessId(UUID requestedBusinessId) {
        if (requestedBusinessId != null) {
            return requestedBusinessId;
        }

        User currentUser = securityUtils.getCurrentUser();
        if (currentUser.getBusinessId() == null) {
            throw new ValidationException("User is not associated with any business");
        }

        return currentUser.getBusinessId();
    }

    private void deactivateCurrentActiveRate(UUID businessId) {
        Optional<BusinessExchangeRate> existingActiveRate = exchangeRateRepository.findActiveRateByBusinessId(businessId);
        if (existingActiveRate.isPresent()) {
            BusinessExchangeRate rate = existingActiveRate.get();
            rate.deactivate();
            exchangeRateRepository.save(rate);
            log.info("Deactivated existing active rate for business {}: {} KHR",
                    businessId, rate.getUsdToKhrRate());
        }
    }
}