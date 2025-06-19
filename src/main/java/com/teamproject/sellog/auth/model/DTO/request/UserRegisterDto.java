package com.teamproject.sellog.auth.model.DTO.request;

import com.fasterxml.jackson.annotation.JsonCreator;

import lombok.Getter;

@Getter
public final class UserRegisterDto {
    private String name;
    private String nickname;
    private String userId;
    private String password;
    private String email;

    @JsonCreator
    public UserRegisterDto(String name, String nickname, String userId, String password, String email) {
        this.name = name;
        this.nickname = nickname;
        this.userId = userId;
        this.password = password;
        this.email = email;
    }
}