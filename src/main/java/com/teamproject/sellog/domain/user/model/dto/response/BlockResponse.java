package com.teamproject.sellog.domain.user.model.dto.response;

import java.sql.Timestamp;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Getter;

@Getter
public class BlockResponse {
    private UUID id;
    private String userId;
    private String nickname;
    private String profileThumbURL;
    private String profileMessage;
    private Timestamp createAt;

    @JsonCreator
    @Builder
    public BlockResponse(@JsonProperty("id") UUID id,
            @JsonProperty("userId") String userId,
            @JsonProperty("nickname") String nickname,
            @JsonProperty("profileThumbURL") String profileThumbURL,
            @JsonProperty("profileMessage") String profileMessage,
            @JsonProperty("createAt") Timestamp createAt) {
        this.userId = userId;
        this.nickname = nickname;
        this.profileThumbURL = profileThumbURL;
        this.profileMessage = profileMessage;
        this.id = id;
        this.createAt = createAt;
    }
}
