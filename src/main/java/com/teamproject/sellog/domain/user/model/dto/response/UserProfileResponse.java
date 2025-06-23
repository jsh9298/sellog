package com.teamproject.sellog.domain.user.model.dto.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.teamproject.sellog.domain.user.model.user.Gender;

import lombok.Builder;
import lombok.Getter;

@Getter
public final class UserProfileResponse {
    private final String profileThumbURL;
    private final String profileURL;
    private final String userName;
    private final String nickname;
    private final Gender gender;
    private final String profileMessage;
    private final String birthDay;
    private final String email;
    private final String phoneNumber;
    private final Integer score;
    private final String userAddress;

    @JsonCreator
    @Builder
    public UserProfileResponse(
            @JsonProperty("profileThumbURL") final String profileThumbURL,
            @JsonProperty("profileURL") final String profileURL,
            @JsonProperty("userName") final String userName,
            @JsonProperty("nickname") final String nickname,
            @JsonProperty("gender") final Gender gender,
            @JsonProperty("profileMessage") final String profileMessage,
            @JsonProperty("birthDay") final String birthDay,
            @JsonProperty("email") final String email,
            @JsonProperty("phoneNumber") final String phoneNumber,
            @JsonProperty("score") final Integer score, @JsonProperty("userAddress") final String userAddress) {
        this.profileThumbURL = profileThumbURL;
        this.profileURL = profileURL;
        this.userName = userName;
        this.nickname = nickname;
        this.gender = gender;
        this.profileMessage = profileMessage;
        this.birthDay = birthDay;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.score = score;
        this.userAddress = userAddress;
    }
}
