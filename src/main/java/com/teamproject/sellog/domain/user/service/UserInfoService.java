package com.teamproject.sellog.domain.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional(readOnly = true)
    public UserProfileResponse findUserProfile(String userId) {

        User user = userRepository.findUserWithProfileAndPrivateByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        UserProfile userProfile = user.getUserProfile();
        UserPrivate userPrivate = user.getUserPrivate();
        UserContentCount userContentCount = userRepository.findContentCountByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return userInfoMapper.EntityToResponse(userProfile, userPrivate, user, userContentCount);
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
        User user = userRepository.findUserWithProfileAndPrivateByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        UserProfile userProfile = user.getUserProfile();
        UserContentCount userContentCount = userRepository.findContentCountByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return userInfoMapper.EntityToResponse(userProfile, userContentCount);
    }
}