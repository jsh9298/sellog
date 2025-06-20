package com.teamproject.sellog.auth.model.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;

import lombok.Getter;

@Getter
public final class RefreshTokenDto {
    private String refreshToken;

    @JsonCreator
    public RefreshTokenDto(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
