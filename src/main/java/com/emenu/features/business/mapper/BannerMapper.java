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

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class BannerMapper {

    @Autowired
    protected PaginationMapper paginationMapper;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "businessId", ignore = true)
    @Mapping(target = "business", ignore = true)
    public abstract Banner toEntity(BannerCreateRequest request);

    @Mapping(source = "business.name", target = "businessName")
    public abstract BannerResponse toResponse(Banner banner);

    public abstract List<BannerResponse> toResponseList(List<Banner> banners);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "businessId", ignore = true)
    @Mapping(target = "business", ignore = true)
    public abstract void updateEntity(BannerUpdateRequest request, @MappingTarget Banner banner);

    public PaginationResponse<BannerResponse> toPaginationResponse(Page<Banner> bannerPage) {
        return paginationMapper.toPaginationResponse(bannerPage, this::toResponseList);
    }
}