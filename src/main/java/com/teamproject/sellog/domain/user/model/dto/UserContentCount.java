package com.teamproject.sellog.domain.user.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public final class UserContentCount {
    private final Long postCount;
    private final Long productCount;
    private final Long followCount; // 팔로윙
    private final Long followedCount; // 팔로워
}