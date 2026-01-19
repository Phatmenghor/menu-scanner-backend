package com.emenu.features.customer.mapper;

import com.emenu.features.customer.dto.request.CustomerAddressCreateRequest;
import com.emenu.features.customer.dto.response.CustomerAddressResponse;
import com.emenu.features.customer.dto.update.CustomerAddressUpdateRequest;
import com.emenu.features.customer.models.CustomerAddress;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", uses = {PaginationMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CustomerAddressMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "user", ignore = true)
    CustomerAddress toEntity(CustomerAddressCreateRequest request);

    @Mapping(target = "fullAddress", expression = "java(address.getFullAddress())")
    @Mapping(target = "hasCoordinates", expression = "java(address.hasCoordinates())")
    CustomerAddressResponse toResponse(CustomerAddress address);

    List<CustomerAddressResponse> toResponseList(List<CustomerAddress> addresses);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "user", ignore = true)
    void updateEntity(CustomerAddressUpdateRequest request, @MappingTarget CustomerAddress address);

    default PaginationResponse<CustomerAddressResponse> toPaginationResponse(Page<CustomerAddress> addresses, PaginationMapper paginationMapper) {
        return paginationMapper.toPaginationResponse(addresses, this::toResponseList);
    }
}