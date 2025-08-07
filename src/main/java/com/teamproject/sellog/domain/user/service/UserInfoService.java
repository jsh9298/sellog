package com.teamproject.sellog.domain.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.teamproject.sellog.common.accountsUtils.CheckStatus;
import com.teamproject.sellog.domain.user.model.dto.UserContentCount;
import com.teamproject.sellog.domain.user.model.dto.request.UserProfileRequest;
import com.teamproject.sellog.domain.user.model.dto.response.UserPreviewResponse;
import com.teamproject.sellog.domain.user.model.dto.response.UserProfileResponse;
import com.teamproject.sellog.domain.user.model.entity.user.AccountStatus;
import com.teamproject.sellog.domain.user.model.entity.user.User;
import com.teamproject.sellog.domain.user.model.entity.user.UserPrivate;
import com.teamproject.sellog.domain.user.model.entity.user.UserProfile;
import com.teamproject.sellog.domain.user.repository.UserRepository;
import com.teamproject.sellog.mapper.UserInfoMapper;

@Service
public class UserInfoService {

    private final UserRepository userRepository;
    private final UserInfoMapper userInfoMapper;

    public UserInfoService(UserRepository userRepository, UserInfoMapper userInfoMapper) {
        this.userRepository = userRepository;
        this.userInfoMapper = userInfoMapper;
    }

    @Transactional(readOnly = true) // 음.. 나중에 리팩토링 가능할수도
    public UserProfileResponse findUserProfile(String userId, String selfId) {
        User user = userRepository.findUserWithProfileAndPrivateByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (CheckStatus.checkSelf(userId, selfId)) {
            UserProfile userProfile = user.getUserProfile();
            UserPrivate userPrivate = user.getUserPrivate();
            UserContentCount userContentCount = userRepository.findContentCountByUserId(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            return userInfoMapper.EntityToResponse(userProfile, userPrivate, user, userContentCount);
        } else {
            User self = userRepository.findUserWithProfileAndPrivateByUserId(selfId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            if (CheckStatus.isBlocking(user, self)) {
                return UserProfileResponse.builder().profileMessage("해당 사용자에게 차단된 상태입니다.").build();
            } else if (CheckStatus.isPrivate(user)) {
                if (CheckStatus.isFollowing(user, self)) {
                    UserProfile userProfile = user.getUserProfile();
                    UserPrivate userPrivate = user.getUserPrivate();
                    UserContentCount userContentCount = userRepository.findContentCountByUserId(userId)
                            .orElseThrow(() -> new IllegalArgumentException("User not found"));
                    return userInfoMapper.EntityToResponse(userProfile, userPrivate, user, userContentCount);
                } else {
                    return UserProfileResponse.builder().profileMessage("이 계정은 비공개 상태입니다.").build();
                }
            } else if (CheckStatus.checkStatus(user) == AccountStatus.INACTIVE) {
                return UserProfileResponse.builder().profileMessage("이 계정은 비활성 상태입니다.").build();
            } else {
                UserProfile userProfile = user.getUserProfile();
                UserPrivate userPrivate = user.getUserPrivate();
                UserContentCount userContentCount = userRepository.findContentCountByUserId(userId)
                        .orElseThrow(() -> new IllegalArgumentException("User not found"));
                return userInfoMapper.EntityToResponse(userProfile, userPrivate, user, userContentCount);
            }
        }
    }

    @Transactional
    public void editUserProfile(String userId, UserProfileRequest userProfileRequest) {
        User user = userRepository.findUserWithProfileAndPrivateByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        UserProfile userProfile = user.getUserProfile();
        UserPrivate userPrivate = user.getUserPrivate();
        if (user.getAccountStatus() == AccountStatus.STAY) {
            user.setAccountStatus(AccountStatus.ACTIVE);
        }
        userInfoMapper.updatePrivateFromRequest(userProfileRequest, userPrivate);
        userInfoMapper.updateProfileFromRequest(userProfileRequest, userProfile);
        userInfoMapper.updateUserFromRequest(userProfileRequest, user);
    }

    @Transactional(readOnly = true)
    public UserPreviewResponse findUserPreview(String userId) {
        UserProfile userProfile = null;
        User user = userRepository.findUserWithProfileAndPrivateByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (CheckStatus.isPrivate(user)) {
            userProfile = new UserProfile();
            userProfile.setNickname(user.getUserProfile().getNickname());
            userProfile.setProfileMessage("이 계정은 비공개 상태입니다.");
            userProfile.setProfileThumbURL(user.getUserProfile().getProfileThumbURL());
            userProfile.setProfileURL(user.getUserProfile().getProfileURL());
            userProfile.setScore(null);
        } else if (CheckStatus.checkStatus(user) == AccountStatus.INACTIVE) {
            userProfile = new UserProfile();
            userProfile.setNickname(user.getUserProfile().getNickname());
            userProfile.setProfileMessage("이 계정은 비활성 상태입니다.");
            userProfile.setProfileThumbURL(user.getUserProfile().getProfileThumbURL());
            userProfile.setProfileURL(user.getUserProfile().getProfileURL());
            userProfile.setScore(null);
        } else {
            userProfile = user.getUserProfile();
        }

        UserContentCount userContentCount = userRepository.findContentCountByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return userInfoMapper.EntityToResponse(userProfile, userContentCount);
    }
}