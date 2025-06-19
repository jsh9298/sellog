package com.teamproject.sellog.auth.model.jwt;

import lombok.Builder;
import lombok.Getter;

@Getter
public final class JWT {
    private String accessToken;
    private String refreshToken;

    @Builder
    public JWT(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}