package com.teamproject.sellog.auth.model.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;

import lombok.Getter;

@Getter
public final class UserLoginDto {
    private String userId;
    private String password;

    @JsonCreator
    public UserLoginDto(String userId, String password) {
        this.userId = userId;
        this.password = password;
    }
}
