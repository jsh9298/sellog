package com.teamproject.sellog.auth.jwt;

import lombok.Builder;
import lombok.Getter;

@Getter
public class JWT {
    private String accessToken;
    private String refreshToken;

    @Builder
    public JWT(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}