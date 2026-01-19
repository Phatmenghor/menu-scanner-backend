package com.emenu.features.hr.mapper;

import com.emenu.features.auth.models.User;
import com.emenu.features.hr.dto.response.UserBasicInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserBasicInfoMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "phoneNumber", source = "phoneNumber")
    @Mapping(target = "profileImageUrl", source = "profileImageUrl")
    UserBasicInfo toUserBasicInfo(User user);
}
