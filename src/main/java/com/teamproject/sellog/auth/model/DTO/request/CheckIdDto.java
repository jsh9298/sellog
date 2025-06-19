package com.teamproject.sellog.auth.model.DTO.request;

import com.fasterxml.jackson.annotation.JsonCreator;

import lombok.Getter;

@Getter
public final class CheckIdDto {
    private String userId;

    @JsonCreator
    public CheckIdDto(String userId) {
        this.userId = userId;
    }
}
