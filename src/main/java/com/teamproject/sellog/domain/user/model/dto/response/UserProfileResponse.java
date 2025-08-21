package com.teamproject.sellog.domain.user.model.dto.response;

import com.teamproject.sellog.common.responseUtils.CursorPageResponse;
import com.teamproject.sellog.domain.user.model.entity.user.Gender;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public final class UserProfileResponse {
    private final String profileThumbURL;
    private final String profileURL;
    private final String userId;
    private final String userName;
    private final String nickname;
    private final Gender gender;
    private final String profileMessage;
    private final String birthDay;
    private final String email;
    private final String phoneNumber;
    private final String userAddress;
    private final Integer score;

    private final Long postCount;
    private final Long productCount;
    private final Long followCount; // 팔로윙
    private final Long followedCount; // 팔로워

    private final CursorPageResponse<SimplePostList> postLists;
}
