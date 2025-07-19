package com.emenu.features.services.mapper;

import com.emenu.features.services.domain.UserSubscription;
import com.emenu.features.services.dto.response.SubscriptionResponse;
import com.emenu.features.services.dto.update.UpdateSubscriptionRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserSubscriptionMapper {
    
    @Mapping(target = "userEmail", ignore = true)
    @Mapping(target = "userName", ignore = true)
    @Mapping(target = "planName", ignore = true)
    @Mapping(target = "maxUsers", ignore = true)
    @Mapping(target = "maxMenus", ignore = true)
    @Mapping(target = "maxOrdersPerMonth", ignore = true)
    SubscriptionResponse toResponse(UserSubscription subscription);
    
    void updateEntity(UpdateSubscriptionRequest request, @MappingTarget UserSubscription subscription);
}