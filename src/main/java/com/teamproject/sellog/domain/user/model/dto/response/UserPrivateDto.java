package com.teamproject.sellog.domain.user.model.dto.response;

import com.fasterxml.jackson.annotation.JsonCreator;

import lombok.Builder;
import lombok.Getter;

@Getter
public final class UserPrivateDto {
    private final String phoneNumber;
    private final String userAddress;
    private final String userName;

    @JsonCreator
    @Builder
    public UserPrivateDto(String phoneNumber, String userAddress, String userName) {
        this.phoneNumber = phoneNumber;
        this.userAddress = userAddress;
        this.userName = userName;
    }
}
