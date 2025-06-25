package com.teamproject.sellog.domain.user.model.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;

import lombok.Getter;

@Getter
public final class OtherUserIdRequest {
    private String otherUserId;

    @JsonCreator
    public OtherUserIdRequest(String otherUserId) {
        this.otherUserId = otherUserId;
    }
}
