package com.teamproject.sellog.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.teamproject.sellog.domain.user.model.dto.request.UserProfileRequest;
import com.teamproject.sellog.domain.user.model.dto.response.UserProfileResponse;
import com.teamproject.sellog.domain.user.model.user.User;
import com.teamproject.sellog.domain.user.model.user.UserPrivate;
import com.teamproject.sellog.domain.user.model.user.UserProfile;

@Mapper(componentModel = "spring")
public interface UserInfoMapper {
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateProfileFromRequest(UserProfileRequest dto, @MappingTarget UserProfile userProfile);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updatePrivateFromRequest(UserProfileRequest dto, @MappingTarget UserPrivate userPrivate);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUserFromRequest(UserProfileRequest dto, @MappingTarget User user);

    UserProfileResponse EntityToResponse(UserProfile userprofile, UserPrivate userprivate, User user);

}
