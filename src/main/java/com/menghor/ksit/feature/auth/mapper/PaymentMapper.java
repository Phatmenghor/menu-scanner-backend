package com.menghor.ksit.feature.auth.mapper;

import com.menghor.ksit.feature.auth.dto.request.PaymentCreateDTO;
import com.menghor.ksit.feature.auth.dto.resposne.PaymentResponseDTO;
import com.menghor.ksit.feature.auth.dto.update.PaymentUpdateDTO;
import com.menghor.ksit.feature.auth.models.PaymentEntity;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "user", source = "userId", qualifiedByName = "mapUserIdToUser")
    PaymentEntity toEntity(PaymentCreateDTO dto);

    // New method for updating an existing entity with non-null values from DTO
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "user", source = "userId", qualifiedByName = "mapUserIdToUser")
    void updateEntityFromDto(PaymentUpdateDTO dto, @MappingTarget PaymentEntity entity);

    @Mapping(target = "userId", source = "user.id")
    PaymentResponseDTO toResponseDto(PaymentEntity entity);

    List<PaymentResponseDTO> toResponseDtoList(List<PaymentEntity> entities);

    default CustomPaginationResponseDto<PaymentResponseDTO> toPaymentAllResponseDto(Page<PaymentEntity> payment) {
        return new CustomPaginationResponseDto<>(
                toResponseDtoList(payment.getContent()),
                payment.getNumber() + 1,
                payment.getSize(),
                payment.getTotalElements(),
                payment.getTotalPages(),
                payment.isLast()
        );
    }

    @Named("mapUserIdToUser")
    default UserEntity mapUserIdToUser(Long userId) {
        if (userId == null) return null;
        UserEntity user = new UserEntity();
        user.setId(userId);
        return user;
    }
}