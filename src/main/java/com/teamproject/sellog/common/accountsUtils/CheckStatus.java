package com.teamproject.sellog.common.accountsUtils;

import com.teamproject.sellog.domain.user.model.entity.user.AccountStatus;
import com.teamproject.sellog.domain.user.model.entity.user.AccountVisibility;
import com.teamproject.sellog.domain.user.model.entity.user.User;

public class CheckStatus { // 상태 체크

    // 대상 계정이 현제 계정을 차단 중인지 체크
    public static boolean isBlocking(User target, User self) {
        return target.getBlocking().stream().anyMatch(block -> block.getBlocked().equals(self));
    }

    // 대상 계정을 팔로우 중인지 체크
    public static boolean isFollowing(User target, User self) {
        return self.getFollowing().stream().anyMatch(follow -> follow.getFollowed().equals(target));
    }

    // 대상 계정이 비공개 인지 체크
    public static boolean isPrivate(User user) {
        return user.getAccountVisibility() == AccountVisibility.PRIVATE;
    }

    // 대상 계정의 활성 상태 리턴
    public static AccountStatus checkStatus(User user) {
        return user.getAccountStatus();
    }

    // 본인 계정 여부 확인
    public static boolean checkSelf(String userId, String selfId) {
        return userId.equals(selfId);
    }
}
