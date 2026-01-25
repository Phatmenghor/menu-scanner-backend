package com.emenu.features.location.mapper;

import com.emenu.features.location.dto.request.CustomerAddressCreateRequest;
import com.emenu.features.location.dto.response.CustomerAddressResponse;
import com.emenu.features.location.dto.update.CustomerAddressUpdateRequest;
import com.emenu.features.location.models.CustomerAddress;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", uses = {PaginationMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CustomerAddressMapper {

    CustomerAddress toEntity(CustomerAddressCreateRequest request);

    @Mapping(target = "fullAddress", expression = "java(address.getFullAddress())")
    @Mapping(target = "hasCoordinates", expression = "java(address.hasCoordinates())")
    CustomerAddressResponse toResponse(CustomerAddress address);

    List<CustomerAddressResponse> toResponseList(List<CustomerAddress> addresses);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(CustomerAddressUpdateRequest request, @MappingTarget CustomerAddress address);

    default PaginationResponse<CustomerAddressResponse> toPaginationResponse(Page<CustomerAddress> addresses, PaginationMapper paginationMapper) {
return paginationMapper.toPaginationResponse(addresses, this::toResponseList);
    }
}