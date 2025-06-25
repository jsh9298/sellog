package com.teamproject.sellog.domain.user.model.dto.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.teamproject.sellog.domain.user.model.entity.user.Gender;

import lombok.Builder;
import lombok.Getter;

@Getter
public final class UserProfileResponse {
    private String profileThumbURL;
    private String profileURL;
    private String userId;
    private String userName;
    private String nickname;
    private Gender gender;
    private String profileMessage;
    private String birthDay;
    private String email;
    private String phoneNumber;
    private String userAddress;
    private Integer score;

    private Long postCount;
    private Long productCount;
    private Long followCount; // 팔로윙
    private Long followedCount; // 팔로워

    @JsonCreator
    @Builder
    public UserProfileResponse(
            @JsonProperty("profileThumbURL") String profileThumbURL,
            @JsonProperty("profileURL") String profileURL,
            @JsonProperty("userId") String userId,
            @JsonProperty("userName") String userName,
            @JsonProperty("nickname") String nickname,
            @JsonProperty("gender") Gender gender,
            @JsonProperty("profileMessage") String profileMessage,
            @JsonProperty("birthDay") String birthDay,
            @JsonProperty("email") String email,
            @JsonProperty("phoneNumber") String phoneNumber,
            @JsonProperty("score") Integer score,
            @JsonProperty("userAddress") String userAddress,
            @JsonProperty("postCount") Long postCount,
            @JsonProperty("productCount") Long productCount,
            @JsonProperty("followCount") Long followCount,
            @JsonProperty("followedCount") Long followedCount) {
        this.profileThumbURL = profileThumbURL;
        this.profileURL = profileURL;
        this.userId = userId;
        this.userName = userName;
        this.nickname = nickname;
        this.gender = gender;
        this.profileMessage = profileMessage;
        this.birthDay = birthDay;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.score = score;
        this.userAddress = userAddress;

        this.postCount = postCount;
        this.productCount = productCount;
        this.followCount = followCount;
        this.followedCount = followedCount;
    }
}
