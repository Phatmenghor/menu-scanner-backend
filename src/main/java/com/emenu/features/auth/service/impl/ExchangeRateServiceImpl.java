package com.emenu.features.auth.service.impl;

import com.emenu.exception.custom.NotFoundException;
import com.emenu.features.auth.dto.filter.ExchangeRateFilterRequest;
import com.emenu.features.auth.dto.request.ExchangeRateCreateRequest;
import com.emenu.features.auth.dto.response.ExchangeRateResponse;
import com.emenu.features.auth.dto.update.ExchangeRateUpdateRequest;
import com.emenu.features.auth.mapper.ExchangeRateMapper;
import com.emenu.features.auth.models.ExchangeRate;
import com.emenu.features.auth.repository.BusinessRepository;
import com.emenu.features.auth.repository.ExchangeRateRepository;
import com.emenu.features.auth.service.ExchangeRateService;
import com.emenu.features.auth.specification.ExchangeRateSpecification;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.pagination.PaginationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ExchangeRateServiceImpl implements ExchangeRateService {

    private final ExchangeRateRepository exchangeRateRepository;
    private final BusinessRepository businessRepository;
    private final ExchangeRateMapper exchangeRateMapper;

    private static final Double DEFAULT_EXCHANGE_RATE = 4000.0;

    @Override
    public ExchangeRateResponse createExchangeRate(ExchangeRateCreateRequest request) {
        log.info("Creating exchange rate: {} for business: {}", request.getUsdToKhrRate(), request.getBusinessId());

        // Validate business exists if businessId is provided
        if (request.getBusinessId() != null) {
            businessRepository.findByIdAndIsDeletedFalse(request.getBusinessId())
                    .orElseThrow(() -> new NotFoundException("Business not found"));
            
            // Deactivate existing business rate if creating a new one
            deactivateExistingBusinessRate(request.getBusinessId());
        } else if (Boolean.TRUE.equals(request.getIsSystemDefault())) {
            // Deactivate existing system default if creating a new one
            deactivateExistingSystemDefault();
        }

        ExchangeRate exchangeRate = exchangeRateMapper.toEntity(request);
        ExchangeRate savedExchangeRate = exchangeRateRepository.save(exchangeRate);

        log.info("Exchange rate created successfully: {}", savedExchangeRate.getId());
        return exchangeRateMapper.toResponse(savedExchangeRate);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<ExchangeRateResponse> getAllExchangeRates(ExchangeRateFilterRequest filter) {
        Specification<ExchangeRate> spec = ExchangeRateSpecification.buildSpecification(filter);

        int pageNo = filter.getPageNo() != null && filter.getPageNo() > 0 ? filter.getPageNo() - 1 : 0;
        Pageable pageable = PaginationUtils.createPageable(
                pageNo, filter.getPageSize(), filter.getSortBy(), filter.getSortDirection()
        );

        Page<ExchangeRate> exchangeRatePage = exchangeRateRepository.findAll(spec, pageable);
        return exchangeRateMapper.toPaginationResponse(exchangeRatePage);
    }

    @Override
    @Transactional(readOnly = true)
    public ExchangeRateResponse getExchangeRateById(UUID id) {
        ExchangeRate exchangeRate = findExchangeRateById(id);
        return exchangeRateMapper.toResponse(exchangeRate);
    }

    @Override
    public ExchangeRateResponse updateExchangeRate(UUID id, ExchangeRateUpdateRequest request) {
        ExchangeRate exchangeRate = findExchangeRateById(id);

        exchangeRateMapper.updateEntity(request, exchangeRate);
        ExchangeRate updatedExchangeRate = exchangeRateRepository.save(exchangeRate);

        log.info("Exchange rate updated successfully: {} - New rate: {}", id, updatedExchangeRate.getUsdToKhrRate());
        return exchangeRateMapper.toResponse(updatedExchangeRate);
    }

    @Override
    public ExchangeRateResponse deleteExchangeRate(UUID id) {
        ExchangeRate exchangeRate = findExchangeRateById(id);

        exchangeRate.softDelete();
        exchangeRate = exchangeRateRepository.save(exchangeRate);

        log.info("Exchange rate deleted successfully: {}", id);
        return exchangeRateMapper.toResponse(exchangeRate);
    }

    @Override
    public ExchangeRateResponse createSystemDefault(Double rate, String notes) {
        log.info("Creating system default exchange rate: {}", rate);

        // Deactivate existing system default
        deactivateExistingSystemDefault();

        ExchangeRate systemDefault = new ExchangeRate();
        systemDefault.setBusinessId(null);
        systemDefault.setUsdToKhrRate(rate);
        systemDefault.setIsSystemDefault(true);
        systemDefault.setIsActive(true);
        systemDefault.setNotes(notes);

        ExchangeRate savedRate = exchangeRateRepository.save(systemDefault);
        log.info("System default exchange rate created: {}", savedRate.getId());

        return exchangeRateMapper.toResponse(savedRate);
    }

    @Override
    @Transactional(readOnly = true)
    public ExchangeRateResponse getActiveSystemDefault() {
        Optional<ExchangeRate> systemDefault = exchangeRateRepository.findActiveSystemDefault();
        
        if (systemDefault.isEmpty()) {
            // Create default system rate if none exists
            log.info("No system default found, creating default rate: {}", DEFAULT_EXCHANGE_RATE);
            return createSystemDefault(DEFAULT_EXCHANGE_RATE, "System generated default rate");
        }

        return exchangeRateMapper.toResponse(systemDefault.get());
    }

    @Override
    public ExchangeRateResponse updateSystemDefault(Double newRate, String notes) {
        log.info("Updating system default exchange rate to: {}", newRate);

        Optional<ExchangeRate> existingDefault = exchangeRateRepository.findActiveSystemDefault();
        
        if (existingDefault.isPresent()) {
            // Update existing system default
            ExchangeRate systemDefault = existingDefault.get();
            systemDefault.setUsdToKhrRate(newRate);
            if (notes != null) {
                systemDefault.setNotes(notes);
            }
            ExchangeRate updatedRate = exchangeRateRepository.save(systemDefault);
            return exchangeRateMapper.toResponse(updatedRate);
        } else {
            // Create new system default
            return createSystemDefault(newRate, notes);
        }
    }

    @Override
    public ExchangeRateResponse createBusinessRate(UUID businessId, Double rate, String notes) {
        log.info("Creating business exchange rate: {} for business: {}", rate, businessId);

        // Validate business exists
        businessRepository.findByIdAndIsDeletedFalse(businessId)
                .orElseThrow(() -> new NotFoundException("Business not found"));

        // Deactivate existing business rate
        deactivateExistingBusinessRate(businessId);

        ExchangeRate businessRate = new ExchangeRate();
        businessRate.setBusinessId(businessId);
        businessRate.setUsdToKhrRate(rate);
        businessRate.setIsSystemDefault(false);
        businessRate.setIsActive(true);
        businessRate.setNotes(notes);

        ExchangeRate savedRate = exchangeRateRepository.save(businessRate);
        log.info("Business exchange rate created: {}", savedRate.getId());

        return exchangeRateMapper.toResponse(savedRate);
    }

    @Override
    @Transactional(readOnly = true)
    public ExchangeRateResponse getActiveBusinessRate(UUID businessId) {
        Optional<ExchangeRate> businessRate = exchangeRateRepository.findActiveByBusinessId(businessId);
        
        if (businessRate.isEmpty()) {
            throw new NotFoundException("No active exchange rate found for business: " + businessId);
        }

        return exchangeRateMapper.toResponse(businessRate.get());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExchangeRateResponse> getBusinessRateHistory(UUID businessId) {
        List<ExchangeRate> rateHistory = exchangeRateRepository.findByBusinessIdAndIsDeletedFalse(businessId);
        return exchangeRateMapper.toResponseList(rateHistory);
    }

    @Override
    public ExchangeRateResponse updateBusinessRate(UUID businessId, Double newRate, String notes) {
        log.info("Updating business exchange rate to: {} for business: {}", newRate, businessId);

        Optional<ExchangeRate> existingRate = exchangeRateRepository.findActiveByBusinessId(businessId);
        
        if (existingRate.isPresent()) {
            // Update existing business rate
            ExchangeRate businessRate = existingRate.get();
            businessRate.setUsdToKhrRate(newRate);
            if (notes != null) {
                businessRate.setNotes(notes);
            }
            ExchangeRate updatedRate = exchangeRateRepository.save(businessRate);
            return exchangeRateMapper.toResponse(updatedRate);
        } else {
            // Create new business rate
            return createBusinessRate(businessId, newRate, notes);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Double getCurrentRate(UUID businessId) {
        if (businessId != null) {
            // Try to get business-specific rate first
            Optional<ExchangeRate> businessRate = exchangeRateRepository.findActiveByBusinessId(businessId);
            if (businessRate.isPresent()) {
                return businessRate.get().getUsdToKhrRate();
            }
        }

        // Fall back to system default
        Optional<ExchangeRate> systemDefault = exchangeRateRepository.findActiveSystemDefault();
        if (systemDefault.isPresent()) {
            return systemDefault.get().getUsdToKhrRate();
        }

        // Ultimate fallback
        log.warn("No exchange rate found, using default: {}", DEFAULT_EXCHANGE_RATE);
        return DEFAULT_EXCHANGE_RATE;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExchangeRateResponse> getAllActiveRates() {
        List<ExchangeRate> activeRates = exchangeRateRepository.findAllActiveRates();
        return exchangeRateMapper.toResponseList(activeRates);
    }

    @Override
    public ExchangeRateResponse activateRate(UUID id) {
        ExchangeRate exchangeRate = findExchangeRateById(id);

        // If this is a business rate, deactivate other business rates
        if (exchangeRate.getBusinessId() != null) {
            deactivateExistingBusinessRate(exchangeRate.getBusinessId());
        }

        // If this is a system default, deactivate other system defaults
        if (exchangeRate.isSystemDefault()) {
            deactivateExistingSystemDefault();
        }

        exchangeRate.setIsActive(true);
        ExchangeRate activatedRate = exchangeRateRepository.save(exchangeRate);

        log.info("Exchange rate activated: {}", id);
        return exchangeRateMapper.toResponse(activatedRate);
    }

    @Override
    public ExchangeRateResponse deactivateRate(UUID id) {
        ExchangeRate exchangeRate = findExchangeRateById(id);

        exchangeRate.setIsActive(false);
        ExchangeRate deactivatedRate = exchangeRateRepository.save(exchangeRate);

        log.info("Exchange rate deactivated: {}", id);
        return exchangeRateMapper.toResponse(deactivatedRate);
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalRatesCount() {
        return exchangeRateRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long getActiveRatesCount() {
        return exchangeRateRepository.findAllActiveRates().size();
    }

    @Override
    @Transactional(readOnly = true)
    public long getBusinessesWithRatesCount() {
        return exchangeRateRepository.countBusinessesWithRates();
    }

    // Private helper methods
    private ExchangeRate findExchangeRateById(UUID id) {
        return exchangeRateRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Exchange rate not found"));
    }

    private void deactivateExistingBusinessRate(UUID businessId) {
        Optional<ExchangeRate> existingRate = exchangeRateRepository.findActiveByBusinessId(businessId);
        if (existingRate.isPresent()) {
            ExchangeRate rate = existingRate.get();
            rate.setIsActive(false);
            exchangeRateRepository.save(rate);
            log.info("Deactivated existing business rate for business: {}", businessId);
        }
    }

    private void deactivateExistingSystemDefault() {
        Optional<ExchangeRate> existingDefault = exchangeRateRepository.findActiveSystemDefault();
        if (existingDefault.isPresent()) {
            ExchangeRate rate = existingDefault.get();
            rate.setIsActive(false);
            exchangeRateRepository.save(rate);
            log.info("Deactivated existing system default rate");
        }
    }
}
