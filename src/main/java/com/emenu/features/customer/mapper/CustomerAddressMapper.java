package com.emenu.features.customer.mapper;

import com.emenu.features.business.dto.response.BrandResponse;
import com.emenu.features.business.models.Brand;
import com.emenu.features.customer.dto.request.CustomerAddressCreateRequest;
import com.emenu.features.customer.dto.response.CustomerAddressResponse;
import com.emenu.features.customer.dto.update.CustomerAddressUpdateRequest;
import com.emenu.features.customer.models.CustomerAddress;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class CustomerAddressMapper {

    @Autowired
    protected PaginationMapper paginationMapper;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "user", ignore = true)
    public abstract CustomerAddress toEntity(CustomerAddressCreateRequest request);

    @Mapping(target = "fullAddress", expression = "java(address.getFullAddress())")
    @Mapping(target = "hasCoordinates", expression = "java(address.hasCoordinates())")
    public abstract CustomerAddressResponse toResponse(CustomerAddress address);

    public abstract List<CustomerAddressResponse> toResponseList(List<CustomerAddress> addresses);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "user", ignore = true)
    public abstract void updateEntity(CustomerAddressUpdateRequest request, @MappingTarget CustomerAddress address);

    public PaginationResponse<CustomerAddressResponse> toPaginationResponse(Page<CustomerAddress> addresses) {
        return paginationMapper.toPaginationResponse(addresses, this::toResponseList);
    }
}