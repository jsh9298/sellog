package com.teamproject.sellog.domain.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.teamproject.sellog.domain.user.model.dto.request.UserProfileRequest;
import com.teamproject.sellog.domain.user.model.dto.response.UserProfileResponse;
import com.teamproject.sellog.domain.user.model.user.User;
import com.teamproject.sellog.domain.user.model.user.UserPrivate;
import com.teamproject.sellog.domain.user.model.user.UserProfile;
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

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        UserProfile userProfile = user.getUserProfile();
        UserPrivate userPrivate = user.getUserPrivate();
        return userInfoMapper.EntityToResponse(userProfile, userPrivate, user);
    }

    @Transactional
    public void editUserProfile(String userId, UserProfileRequest userProfileRequest) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        UserProfile userProfile = user.getUserProfile();
        UserPrivate userPrivate = user.getUserPrivate();
        userInfoMapper.updatePrivateFromRequest(userProfileRequest, userPrivate);
        userInfoMapper.updateProfileFromRequest(userProfileRequest, userProfile);
        userInfoMapper.updateUserFromRequest(userProfileRequest, user);
    }
}