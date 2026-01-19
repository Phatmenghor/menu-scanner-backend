package com.emenu.features.customer.service.impl;

import com.emenu.exception.custom.NotFoundException;
import com.emenu.exception.custom.ValidationException;
import com.emenu.features.auth.models.User;
import com.emenu.features.customer.dto.filter.CustomerAddressFilterRequest;
import com.emenu.features.customer.dto.request.CustomerAddressCreateRequest;
import com.emenu.features.customer.dto.response.CustomerAddressResponse;
import com.emenu.features.customer.dto.update.CustomerAddressUpdateRequest;
import com.emenu.features.customer.mapper.CustomerAddressMapper;
import com.emenu.features.customer.models.CustomerAddress;
import com.emenu.features.customer.repository.CustomerAddressRepository;
import com.emenu.features.customer.service.CustomerAddressService;
import com.emenu.features.customer.specification.CustomerAddressSpecification;
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
public class CustomerAddressServiceImpl implements CustomerAddressService {

    private final CustomerAddressRepository addressRepository;
    private final CustomerAddressMapper addressMapper;
    private final SecurityUtils securityUtils;
    private final com.emenu.shared.mapper.PaginationMapper paginationMapper;

    @Override
    public CustomerAddressResponse createAddress(CustomerAddressCreateRequest request) {
        User currentUser = securityUtils.getCurrentUser();
        
        CustomerAddress address = addressMapper.toEntity(request);
        address.setUserId(currentUser.getId());
        
        // If this is set as default or no default exists, make it default
        if (request.getIsDefault() || !hasDefaultAddress(currentUser.getId())) {
            clearDefaultForUser(currentUser.getId());
            address.setAsDefault();
        }
        
        CustomerAddress savedAddress = addressRepository.save(address);
        log.info("Address created for user: {}", currentUser.getUserIdentifier());
        
        return addressMapper.toResponse(savedAddress);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<CustomerAddressResponse> getAllAddresses(CustomerAddressFilterRequest filter) {
        
        Specification<CustomerAddress> spec = CustomerAddressSpecification.buildSpecification(filter);
        
        Pageable pageable = PaginationUtils.createPageable(
                filter.getPageNo(), filter.getPageSize(), filter.getSortBy(), filter.getSortDirection()
        );

        Page<CustomerAddress> addressPage = addressRepository.findAll(spec, pageable);
        return addressMapper.toPaginationResponse(addressPage, paginationMapper);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerAddressResponse> getMyAddressesList() {
        User currentUser = securityUtils.getCurrentUser();
        List<CustomerAddress> addresses = addressRepository
                .findByUserIdAndIsDeletedFalseOrderByIsDefaultDescCreatedAtDesc(currentUser.getId());
        return addressMapper.toResponseList(addresses);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerAddressResponse> getMyAddresses() {
        // Deprecated method - use getMyAddressesList() instead
        return getMyAddressesList();
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerAddressResponse getAddressById(UUID id) {
        User currentUser = securityUtils.getCurrentUser();
        CustomerAddress address = addressRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Address not found"));
        
        if (!address.getUserId().equals(currentUser.getId())) {
            throw new ValidationException("You can only access your own addresses");
        }
        
        return addressMapper.toResponse(address);
    }

    @Override
    public CustomerAddressResponse updateAddress(UUID id, CustomerAddressUpdateRequest request) {
        User currentUser = securityUtils.getCurrentUser();
        CustomerAddress address = addressRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Address not found"));
        
        if (!address.getUserId().equals(currentUser.getId())) {
            throw new ValidationException("You can only update your own addresses");
        }
        
        addressMapper.updateEntity(request, address);
        
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefaultForUser(currentUser.getId());
            address.setAsDefault();
        }
        
        CustomerAddress updatedAddress = addressRepository.save(address);
        return addressMapper.toResponse(updatedAddress);
    }

    @Override
    public CustomerAddressResponse deleteAddress(UUID id) {
        User currentUser = securityUtils.getCurrentUser();
        CustomerAddress address = addressRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Address not found"));
        
        if (!address.getUserId().equals(currentUser.getId())) {
            throw new ValidationException("You can only delete your own addresses");
        }
        
        address.softDelete();
        addressRepository.save(address);
        
        log.info("Address deleted for user: {}", currentUser.getUserIdentifier());
        return addressMapper.toResponse(address);
    }

    @Override
    public CustomerAddressResponse setDefaultAddress(UUID id) {
        User currentUser = securityUtils.getCurrentUser();
        CustomerAddress address = addressRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Address not found"));
        
        if (!address.getUserId().equals(currentUser.getId())) {
            throw new ValidationException("You can only set your own addresses as default");
        }
        
        clearDefaultForUser(currentUser.getId());
        address.setAsDefault();
        
        CustomerAddress updatedAddress = addressRepository.save(address);
        return addressMapper.toResponse(updatedAddress);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerAddressResponse getDefaultAddress() {
        User currentUser = securityUtils.getCurrentUser();
        CustomerAddress defaultAddress = addressRepository
                .findByUserIdAndIsDefaultTrueAndIsDeletedFalse(currentUser.getId())
                .orElseThrow(() -> new NotFoundException("No default address found"));
        
        return addressMapper.toResponse(defaultAddress);
    }
    
    private boolean hasDefaultAddress(UUID userId) {
        return addressRepository.findByUserIdAndIsDefaultTrueAndIsDeletedFalse(userId).isPresent();
    }
    
    private void clearDefaultForUser(UUID userId) {
        addressRepository.clearDefaultForUser(userId);
    }
}