package com.emenu.features.business.mapper;

import com.emenu.features.business.dto.request.BannerCreateRequest;
import com.emenu.features.business.dto.response.BannerResponse;
import com.emenu.features.business.dto.update.BannerUpdateRequest;
import com.emenu.features.business.models.Banner;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", uses = {PaginationMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BannerMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "businessId", ignore = true)
    @Mapping(target = "business", ignore = true)
    Banner toEntity(BannerCreateRequest request);

    @Mapping(source = "business.name", target = "businessName")
    BannerResponse toResponse(Banner banner);

    List<BannerResponse> toResponseList(List<Banner> banners);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "businessId", ignore = true)
    @Mapping(target = "business", ignore = true)
    void updateEntity(BannerUpdateRequest request, @MappingTarget Banner banner);

    default PaginationResponse<BannerResponse> toPaginationResponse(Page<Banner> bannerPage, PaginationMapper paginationMapper) {
        return paginationMapper.toPaginationResponse(bannerPage, this::toResponseList);
    }
}