package com.teamproject.sellog.domain.user.model.dto.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Getter;

@Getter
public final class UserPrivateDto {
    private final String phoneNumber;
    private final String userAddress;
    private final String userName;

    @JsonCreator
    @Builder
    public UserPrivateDto(@JsonProperty("phoneNumber") final String phoneNumber,
            @JsonProperty("userAddress") final String userAddress, @JsonProperty("userName") final String userName) {
        this.phoneNumber = phoneNumber;
        this.userAddress = userAddress;
        this.userName = userName;
    }
}
