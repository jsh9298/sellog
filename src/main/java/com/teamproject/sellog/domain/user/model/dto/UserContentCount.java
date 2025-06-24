package com.teamproject.sellog.domain.user.model.dto;

import lombok.Getter;

@Getter
public final class UserContentCount {
    private final Long postCount;
    private final Long productCount;
    private final Long followCount; // 팔로윙
    private final Long followedCount; // 팔로워

    public UserContentCount(final Long postCount, final Long productCount, final Long followCount,
            final Long followedCount) {
        this.postCount = postCount;
        this.productCount = productCount;
        this.followCount = followCount;
        this.followedCount = followedCount;
    }
}