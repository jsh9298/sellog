package com.teamproject.sellog.auth.model.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;

import lombok.Getter;

@Getter
public final class UserPasswordDto {
    private String userId;
    private String email;
    private String password;

    @JsonCreator
    public UserPasswordDto(String userId, String email, String password) {
        this.userId = userId;
        this.email = email;
        this.password = password;
    }
}
