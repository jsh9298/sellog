package com.teamproject.sellog.auth.model.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
public class UserLoginResponse {
    private String accessToken;
    private String refreshToken;
    private String profileThumbURL;

    @Builder
    public UserLoginResponse(String accessToken, String refreshToken, String profileThumbURL) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.profileThumbURL = profileThumbURL;
    }
}