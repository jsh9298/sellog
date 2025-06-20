package com.teamproject.sellog.domain.user.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.teamproject.sellog.auth.repository.AuthRepository;
import com.teamproject.sellog.domain.user.model.dto.response.UserPrivateDto;
import com.teamproject.sellog.domain.user.model.dto.response.UserProfileDto;
import com.teamproject.sellog.domain.user.model.user.UserPrivate;
import com.teamproject.sellog.domain.user.model.user.UserProfile;
import com.teamproject.sellog.domain.user.repository.UserPrivateRepository;
import com.teamproject.sellog.domain.user.repository.UserProfileRepository;

@Service
public class UserService {

    private final AuthRepository authRepository;
    private final UserPrivateRepository userPrivateRepository;
    private final UserProfileRepository userProfileRepository;

    public UserService(AuthRepository authRepository, UserPrivateRepository userPrivateRepository,
            UserProfileRepository userProfileRepository) {
        this.authRepository = authRepository;
        this.userPrivateRepository = userPrivateRepository;
        this.userProfileRepository = userProfileRepository;
    }

    public UserProfileDto findUserProfile(String userId) {
        UserProfile response = userProfileRepository.findById(getId(userId)).orElseThrow();
        return UserProfileDto
                .builder()
                .nickname(response.getNickname())
                .profileMessage(response.getProfileMessage())
                .profileThumbURL(response.getProfileThumbURL())
                .profileURL(response.getProfileURL())
                .score(response.getScore())
                .build();
    }

    public UserPrivateDto findUserPrivate(String userId) {
        UserPrivate response = userPrivateRepository.findById(getId(userId)).orElseThrow();
        return UserPrivateDto
                .builder()
                .phoneNumber(response.getPhoneNumber())
                .userAddress(response.getUserAddress())
                .userName(response.getUserName())
                .build();
    }

    private UUID getId(String userId) {
        return authRepository.findIdByUserId(userId).orElseThrow();
    }
}