package com.teamproject.sellog.domain.user.model.dto.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Getter;

@Getter
public class UserPreviewResponse {
    private final String profileThumbURL;
    private final String profileURL;
    private final String nickname;
    private final String profileMessage;
    private final Integer score;
    private final Long postCount;
    private final Long productCount;
    private final Long followCount; // 팔로윙
    private final Long followedCount; // 팔로워

    @JsonCreator
    @Builder
    public UserPreviewResponse(
            @JsonProperty("profileThumbURL") final String profileThumbURL,
            @JsonProperty("profileURL") final String profileURL,
            @JsonProperty("nickname") final String nickname,
            @JsonProperty("profileMessage") final String profileMessage,
            @JsonProperty("score") final Integer score,
            @JsonProperty("postCount") final Long postCount,
            @JsonProperty("productCount") final Long productCount,
            @JsonProperty("followCount") final Long followCount,
            @JsonProperty("followedCount") final Long followedCount) {
        this.profileThumbURL = profileThumbURL;
        this.profileURL = profileURL;
        this.nickname = nickname;
        this.profileMessage = profileMessage;
        this.score = score;

        this.postCount = postCount;
        this.productCount = productCount;
        this.followCount = followCount;
        this.followedCount = followedCount;
    }
}
