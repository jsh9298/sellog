package com.teamproject.sellog.domain.user.api.graphql;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import com.teamproject.sellog.domain.user.model.DTO.request.InfoInput;
import com.teamproject.sellog.domain.user.model.DTO.response.Response;
import com.teamproject.sellog.domain.user.service.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class UserResolver {
    private final UserService userService;

    @QueryMapping
    public Response userInfoByUserId(@Argument String userId) {
        return userService.getUserInfo(userId);
    }

    @QueryMapping
    public Response userPrivateByUserId(@Argument String userId) {
        return userService.getUserPrivate(userId);
    }

    @QueryMapping
    public Response userProfileByUserId(@Argument String userId) {
        return userService.getUserProfile(userId);
    }

    @MutationMapping
    public Response userInfoSetting(@Argument InfoInput input, @Argument String userId) {
        return userService.setUserInfo(input, userId);
    }

    @MutationMapping
    public Response userPrivateSetting(@Argument InfoInput input, @Argument String userId) {
        return userService.setUserPrivate(input, userId);
    }

    @MutationMapping
    public Response userProfileSetting(@Argument InfoInput input, @Argument String userId) {
        return userService.setUserProfile(input, userId);
    }
}
