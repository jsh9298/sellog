package com.teamproject.sellog.domain.user.model.dto.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Getter;

@Getter
public final class UserPreviewResponse {
    private String profileThumbURL;
    private String profileURL;
    private String userId;
    private String nickname;
    private String profileMessage;
    private Integer score;
    private Long postCount;
    private Long productCount;
    private Long followCount; // 팔로윙
    private Long followedCount; // 팔로워

    @JsonCreator
    @Builder
    public UserPreviewResponse(
            @JsonProperty("profileThumbURL") String profileThumbURL,
            @JsonProperty("profileURL") String profileURL,
            @JsonProperty("userId") String userId,
            @JsonProperty("nickname") String nickname,
            @JsonProperty("profileMessage") String profileMessage,
            @JsonProperty("score") Integer score,
            @JsonProperty("postCount") Long postCount,
            @JsonProperty("productCount") Long productCount,
            @JsonProperty("followCount") Long followCount,
            @JsonProperty("followedCount") Long followedCount) {
        this.profileThumbURL = profileThumbURL;
        this.profileURL = profileURL;
        this.userId = userId;
        this.nickname = nickname;
        this.profileMessage = profileMessage;
        this.score = score;

        this.postCount = postCount;
        this.productCount = productCount;
        this.followCount = followCount;
        this.followedCount = followedCount;
    }
}
