package com.teamproject.sellog.domain.user.api.graphql;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import com.teamproject.sellog.domain.user.model.DTO.request.UserProfileDto;
import com.teamproject.sellog.domain.user.model.user.UserProfile;
import com.teamproject.sellog.domain.user.service.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class UserResolver {
    private final UserService userService;

    // @QueryMapping
    // public UserProfile getUserProfile(@Argument String userId) {
    // return userService.getUserProfile(userId);
    // }

    // @MutationMapping
    // public UserProfile updateUserProfile(@Argument String userId, @Argument
    // UserProfileDto userProfileDTO) {
    // return userService.updateUserProfile(userId, userProfileDTO);
    // }
}
