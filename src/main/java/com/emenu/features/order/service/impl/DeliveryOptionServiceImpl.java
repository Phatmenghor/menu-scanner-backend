package com.emenu.features.order.service.impl;

import com.emenu.enums.common.Status;
import com.emenu.exception.custom.NotFoundException;
import com.emenu.exception.custom.ValidationException;
import com.emenu.features.auth.models.User;
import com.emenu.features.order.dto.request.DeliveryOptionCreateRequest;
import com.emenu.features.order.dto.response.DeliveryOptionResponse;
import com.emenu.features.order.dto.update.DeliveryOptionUpdateRequest;
import com.emenu.features.order.mapper.DeliveryOptionMapper;
import com.emenu.features.order.models.DeliveryOption;
import com.emenu.features.order.repository.DeliveryOptionRepository;
import com.emenu.features.order.service.DeliveryOptionService;
import com.emenu.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DeliveryOptionServiceImpl implements DeliveryOptionService {

    private final DeliveryOptionRepository deliveryOptionRepository;
    private final DeliveryOptionMapper deliveryOptionMapper;
    private final SecurityUtils securityUtils;

    @Override
    public DeliveryOptionResponse createDeliveryOption(DeliveryOptionCreateRequest request) {
        User currentUser = securityUtils.getCurrentUser();
        if (currentUser.getBusinessId() == null) {
            throw new ValidationException("User is not associated with any business");
        }

        DeliveryOption deliveryOption = deliveryOptionMapper.toEntity(request);
        deliveryOption.setBusinessId(currentUser.getBusinessId());

        DeliveryOption savedDeliveryOption = deliveryOptionRepository.save(deliveryOption);
        log.info("Delivery option created: {} for business: {}", savedDeliveryOption.getName(), currentUser.getBusinessId());

        return deliveryOptionMapper.toResponse(savedDeliveryOption);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeliveryOptionResponse> getMyBusinessDeliveryOptions() {
        User currentUser = securityUtils.getCurrentUser();
        if (currentUser.getBusinessId() == null) {
            throw new ValidationException("User is not associated with any business");
        }

        List<DeliveryOption> deliveryOptions = deliveryOptionRepository
                .findByBusinessIdAndIsDeletedFalseOrderByCreatedAtDesc(currentUser.getBusinessId());
        return deliveryOptionMapper.toResponseList(deliveryOptions);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeliveryOptionResponse> getActiveDeliveryOptions(UUID businessId) {
        List<DeliveryOption> deliveryOptions = deliveryOptionRepository
                .findActiveByBusinessId(businessId, Status.ACTIVE);
        return deliveryOptionMapper.toResponseList(deliveryOptions);
    }

    @Override
    @Transactional(readOnly = true)
    public DeliveryOptionResponse getDeliveryOptionById(UUID id) {
        DeliveryOption deliveryOption = deliveryOptionRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Delivery option not found"));
        return deliveryOptionMapper.toResponse(deliveryOption);
    }

    @Override
    public DeliveryOptionResponse updateDeliveryOption(UUID id, DeliveryOptionUpdateRequest request) {
        DeliveryOption deliveryOption = deliveryOptionRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Delivery option not found"));

        deliveryOptionMapper.updateEntity(request, deliveryOption);
        DeliveryOption updatedDeliveryOption = deliveryOptionRepository.save(deliveryOption);

        log.info("Delivery option updated: {}", id);
        return deliveryOptionMapper.toResponse(updatedDeliveryOption);
    }

    @Override
    public DeliveryOptionResponse deleteDeliveryOption(UUID id) {
        DeliveryOption deliveryOption = deliveryOptionRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Delivery option not found"));

        deliveryOption.softDelete();
        deliveryOption = deliveryOptionRepository.save(deliveryOption);

        log.info("Delivery option deleted: {}", id);
        return deliveryOptionMapper.toResponse(deliveryOption);
    }
}