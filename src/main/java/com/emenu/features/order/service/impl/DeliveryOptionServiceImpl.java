package com.emenu.features.order.service.impl;

import com.emenu.enums.common.Status;
import com.emenu.exception.custom.NotFoundException;
import com.emenu.exception.custom.ValidationException;
import com.emenu.features.auth.models.User;
import com.emenu.features.order.dto.filter.DeliveryOptionFilterRequest;
import com.emenu.features.order.dto.request.DeliveryOptionCreateRequest;
import com.emenu.features.order.dto.response.DeliveryOptionResponse;
import com.emenu.features.order.dto.update.DeliveryOptionUpdateRequest;
import com.emenu.features.order.mapper.DeliveryOptionMapper;
import com.emenu.features.order.models.DeliveryOption;
import com.emenu.features.order.repository.DeliveryOptionRepository;
import com.emenu.features.order.service.DeliveryOptionService;
import com.emenu.features.order.specification.DeliveryOptionSpecification;
import com.emenu.security.SecurityUtils;
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
        log.info("Creating delivery option: {}", request.getName());

        User currentUser = securityUtils.getCurrentUser();
        validateUserBusinessAssociation(currentUser);

        // Check if name already exists for this business
        if (deliveryOptionRepository.existsByNameAndBusinessIdAndIsDeletedFalse(
                request.getName(), currentUser.getBusinessId())) {
            throw new ValidationException("Delivery option name already exists in your business");
        }

        DeliveryOption deliveryOption = deliveryOptionMapper.toEntity(request);
        deliveryOption.setBusinessId(currentUser.getBusinessId());

        DeliveryOption savedDeliveryOption = deliveryOptionRepository.save(deliveryOption);

        log.info("Delivery option created successfully: {} for business: {}",
                savedDeliveryOption.getName(), currentUser.getBusinessId());

        return deliveryOptionMapper.toResponse(savedDeliveryOption);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<DeliveryOptionResponse> getAllDeliveryOptions(DeliveryOptionFilterRequest filter) {

        // Security: Business users can only see delivery options from their business
        User currentUser = securityUtils.getCurrentUser();
        if (currentUser.isBusinessUser() && filter.getBusinessId() == null) {
            filter.setBusinessId(currentUser.getBusinessId());
        }

        Specification<DeliveryOption> spec = DeliveryOptionSpecification.buildSpecification(filter);

        int pageNo = filter.getPageNo() != null && filter.getPageNo() > 0 ? filter.getPageNo() - 1 : 0;
        Pageable pageable = PaginationUtils.createPageable(
                pageNo, filter.getPageSize(), filter.getSortBy(), filter.getSortDirection()
        );

        Page<DeliveryOption> deliveryOptionPage = deliveryOptionRepository.findAll(spec, pageable);
        return deliveryOptionMapper.toPaginationResponse(deliveryOptionPage);
    }

    @Override
    @Transactional(readOnly = true)
    public DeliveryOptionResponse getDeliveryOptionById(UUID id) {
        DeliveryOption deliveryOption = findDeliveryOptionById(id);
        return deliveryOptionMapper.toResponse(deliveryOption);
    }

    @Override
    public DeliveryOptionResponse updateDeliveryOption(UUID id, DeliveryOptionUpdateRequest request) {
        DeliveryOption deliveryOption = findDeliveryOptionById(id);

        // Check if new name already exists (if name is being changed)
        if (request.getName() != null && !request.getName().equals(deliveryOption.getName())) {
            if (deliveryOptionRepository.existsByNameAndBusinessIdAndIsDeletedFalse(
                    request.getName(), deliveryOption.getBusinessId())) {
                throw new ValidationException("Delivery option name already exists in your business");
            }
        }

        deliveryOptionMapper.updateEntity(request, deliveryOption);
        DeliveryOption updatedDeliveryOption = deliveryOptionRepository.save(deliveryOption);

        log.info("Delivery option updated successfully: {}", id);
        return deliveryOptionMapper.toResponse(updatedDeliveryOption);
    }

    @Override
    public DeliveryOptionResponse deleteDeliveryOption(UUID id) {
        DeliveryOption deliveryOption = findDeliveryOptionById(id);

        deliveryOption.softDelete();
        deliveryOption = deliveryOptionRepository.save(deliveryOption);

        log.info("Delivery option deleted successfully: {}", id);
        return deliveryOptionMapper.toResponse(deliveryOption);
    }

    // ================================
    // PRIVATE HELPER METHODS
    // ================================

    private DeliveryOption findDeliveryOptionById(UUID id) {
        return deliveryOptionRepository.findByIdWithBusiness(id)
                .orElseThrow(() -> new NotFoundException("Delivery option not found"));
    }

    private void validateUserBusinessAssociation(User user) {
        if (user.getBusinessId() == null) {
            throw new ValidationException("User is not associated with any business");
        }
    }
}