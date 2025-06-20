package com.teamproject.sellog.auth.model.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;

import lombok.Getter;

// 회원 탈퇴 요청 DTO
@Getter

public final class UserDeleteDto {

    private String userId;
    private String password;

    @JsonCreator
    public UserDeleteDto(String userId,
            String password) {
        this.userId = userId;
        this.password = password;
    }
}
