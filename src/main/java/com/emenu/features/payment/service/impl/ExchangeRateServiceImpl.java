package com.emenu.features.payment.service.impl;

import com.emenu.exception.custom.NotFoundException;
import com.emenu.features.payment.dto.filter.ExchangeRateFilterRequest;
import com.emenu.features.payment.dto.request.ExchangeRateCreateRequest;
import com.emenu.features.payment.dto.response.ExchangeRateResponse;
import com.emenu.features.payment.dto.update.ExchangeRateUpdateRequest;
import com.emenu.features.payment.mapper.ExchangeRateMapper;
import com.emenu.features.payment.models.ExchangeRate;
import com.emenu.features.payment.repository.ExchangeRateRepository;
import com.emenu.features.payment.service.ExchangeRateService;
import com.emenu.features.payment.specification.ExchangeRateSpecification;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.pagination.PaginationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ExchangeRateServiceImpl implements ExchangeRateService {

    private final ExchangeRateRepository exchangeRateRepository;
    private final ExchangeRateMapper exchangeRateMapper;
    private static final Double DEFAULT_EXCHANGE_RATE = 4000.00;

    @Override
    public ExchangeRateResponse createExchangeRate(ExchangeRateCreateRequest request) {
        log.info("Creating new system exchange rate: {}", request.getUsdToKhrRate());

        // Deactivate existing active rate
        deactivateCurrentActiveRate();

        ExchangeRate exchangeRate = exchangeRateMapper.toEntity(request);
        exchangeRate.setIsActive(true); // New rate is always active

        ExchangeRate savedExchangeRate = exchangeRateRepository.save(exchangeRate);

        log.info("System exchange rate created successfully: {} KHR per USD", savedExchangeRate.getUsdToKhrRate());
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

        // Don't allow deletion of the only active rate
        if (exchangeRate.getIsActive() && exchangeRateRepository.countActiveRates() == 1) {
            throw new RuntimeException("Cannot delete the only active exchange rate. Create a new rate first.");
        }

        exchangeRate.softDelete();
        exchangeRate = exchangeRateRepository.save(exchangeRate);

        log.info("Exchange rate deleted successfully: {}", id);
        return exchangeRateMapper.toResponse(exchangeRate);
    }

    @Override
    @Transactional(readOnly = true)
    public ExchangeRateResponse getCurrentActiveRate() {
        ExchangeRate activeRate = exchangeRateRepository.findActiveRate().orElseThrow(
                () -> new NotFoundException("No active exchange rate found")
        );

        return exchangeRateMapper.toResponse(activeRate);
    }

    @Override
    @Transactional(readOnly = true)
    public Double getCurrentRateValue() {
        Optional<ExchangeRate> activeRate = exchangeRateRepository.findActiveRate();

        if (activeRate.isPresent()) {
            return activeRate.get().getUsdToKhrRate();
        }

        // Return default rate if no active rate exists
        log.warn("No active exchange rate found, using default: {}", DEFAULT_EXCHANGE_RATE);
        return DEFAULT_EXCHANGE_RATE;
    }

    // Private helper methods
    private ExchangeRate findExchangeRateById(UUID id) {
        return exchangeRateRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Exchange rate not found"));
    }

    private void deactivateCurrentActiveRate() {
        Optional<ExchangeRate> existingActiveRate = exchangeRateRepository.findActiveRate();
        if (existingActiveRate.isPresent()) {
            ExchangeRate rate = existingActiveRate.get();
            rate.setIsActive(false);
            exchangeRateRepository.save(rate);
            log.info("Deactivated existing active rate: {}", rate.getUsdToKhrRate());
        }
    }

}