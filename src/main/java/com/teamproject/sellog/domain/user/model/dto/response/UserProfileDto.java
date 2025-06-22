package com.teamproject.sellog.domain.user.model.dto.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Getter;

@Getter
public final class UserProfileDto {
    private final String nickname;
    private final String profileMessage;
    private final Integer score;
    private final String profileThumbURL;
    private final String profileURL;

    @JsonCreator
    @Builder
    public UserProfileDto(@JsonProperty("nickname") final String nickname,
            @JsonProperty("profileMessage") final String profileMessage,
            @JsonProperty("score") final Integer score,
            @JsonProperty("profileThumbURL") final String profileThumbURL,
            @JsonProperty("profileURL") final String profileURL) {
        this.nickname = nickname;
        this.profileMessage = profileMessage;
        this.score = score;
        this.profileThumbURL = profileThumbURL;
        this.profileURL = profileURL;
    }

}
