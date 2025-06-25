package com.teamproject.sellog.domain.user.model.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.teamproject.sellog.domain.user.model.entity.user.Gender;

import lombok.Getter;

@Getter
public final class UserProfileRequest {
    private String profileThumbURL;
    private String profileURL;
    private String userName;
    private String nickname;
    private Gender gender;
    private String profileMessage;
    private String birthDay;
    private String email;
    private String phoneNumber;
    private String userAddress;

    @JsonCreator
    public UserProfileRequest(String profileThumbURL, String profileURL, String userName,
            String nickname,
            Gender gender, String profileMessage, String birthDay, String email,
            String phoneNumber, String userAddress) {
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
