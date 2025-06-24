package com.teamproject.sellog.domain.user.model.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.teamproject.sellog.domain.user.model.entity.user.Gender;

import lombok.Getter;

@Getter
public final class UserProfileRequest {
    private final String profileThumbURL;
    private final String profileURL;
    private final String userName;
    private final String nickname;
    private final Gender gender;
    private final String profileMessage;
    private final String birthDay;
    private final String email;
    private final String phoneNumber;
    private final String userAddress;

    @JsonCreator
    public UserProfileRequest(final String profileThumbURL, final String profileURL, final String userName,
            final String nickname,
            final Gender gender, final String profileMessage, final String birthDay, final String email,
            final String phoneNumber, final String userAddress) {
        this.profileThumbURL = profileThumbURL;
        this.profileURL = profileURL;
        this.userName = userName;
        this.nickname = nickname;
        this.gender = gender;
        this.profileMessage = profileMessage;
        this.birthDay = birthDay;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.userAddress = userAddress;

    }
}
