package com.teamproject.sellog.domain.user.model.dto;

import lombok.Getter;

@Getter
public final class UserContentCount {
    private Long postCount;
    private Long productCount;
    private Long followCount; // 팔로윙
    private Long followedCount; // 팔로워

    public UserContentCount(Long postCount, Long productCount, Long followCount,
            Long followedCount) {
        this.postCount = postCount;
        this.productCount = productCount;
        this.followCount = followCount;
        this.followedCount = followedCount;
    }
}