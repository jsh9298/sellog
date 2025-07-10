package com.teamproject.sellog.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.teamproject.sellog.domain.user.model.dto.UserContentCount;
import com.teamproject.sellog.domain.user.model.dto.request.UserProfileRequest;
import com.teamproject.sellog.domain.user.model.dto.response.UserPreviewResponse;
import com.teamproject.sellog.domain.user.model.dto.response.UserProfileResponse;
import com.teamproject.sellog.domain.user.model.entity.user.User;
import com.teamproject.sellog.domain.user.model.entity.user.UserPrivate;
import com.teamproject.sellog.domain.user.model.entity.user.UserProfile;

@Mapper(componentModel = "spring")
public interface UserInfoMapper {
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)

    @Mapping(source = "nickname", target = "nickname")
    @Mapping(source = "profileMessage", target = "profileMessage")
    @Mapping(source = "profileThumbURL", target = "profileThumbURL")
    @Mapping(source = "profileURL", target = "profileURL")
    void updateProfileFromRequest(UserProfileRequest dto, @MappingTarget UserProfile userProfile);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(source = "userName", target = "userName")
    @Mapping(source = "gender", target = "gender")
    @Mapping(source = "birthDay", target = "birthDay")
    @Mapping(source = "phoneNumber", target = "phoneNumber")
    @Mapping(source = "userAddress", target = "userAddress")
    void updatePrivateFromRequest(UserProfileRequest dto, @MappingTarget UserPrivate userPrivate);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUserFromRequest(UserProfileRequest dto, @MappingTarget User user);

    UserProfileResponse EntityToResponse(UserProfile userprofile, UserPrivate userprivate, User user,
            UserContentCount userContentCount);

    UserPreviewResponse EntityToResponse(UserProfile userprofile, UserContentCount userContentCount);
}
