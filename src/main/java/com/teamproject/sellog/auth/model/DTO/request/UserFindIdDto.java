package com.teamproject.sellog.auth.model.DTO.request;

import lombok.Getter;
import com.fasterxml.jackson.annotation.JsonCreator;

@Getter
public final class UserFindIdDto {
    private String username;
    private String email;

    @JsonCreator
    public UserFindIdDto(String username, String email) {
        this.username = username;
        this.email = email;
    }
}
