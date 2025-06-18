package com.teamproject.sellog.domain.user.api.graphql;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import com.teamproject.sellog.domain.user.model.DTO.request.InfoInput;
import com.teamproject.sellog.domain.user.model.DTO.response.Response;
import com.teamproject.sellog.domain.user.model.user.UserProfile;
import com.teamproject.sellog.domain.user.service.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class UserResolver {
    private final UserService userService;

    @QueryMapping
    public InfoInput userInfoByUserId(@Argument String userId) {
        return userService.getUserInfo(userId);
    }

    @QueryMapping
    public InfoInput userPrivateByUserId(@Argument String userId) {
        return userService.getUserPrivate(userId);
    }

    @QueryMapping
    public InfoInput userProfileByUserId(@Argument String userId) {
        return userService.getUserProfile(userId);
    }

    @MutationMapping
    public InfoInput userInfoSetting(@Argument Response input, @Argument String userId) {
        return userService.setUserInfo(input, userId);
    }

    @MutationMapping
    public InfoInput userPrivateSetting(@Argument Response input, @Argument String userId) {
        return userService.setUserPrivate(input, userId);
    }

    @MutationMapping
    public InfoInput userProfileSetting(@Argument Response input, @Argument String userId) {
        return userService.setUserProfile(input, userId);
    }
}
