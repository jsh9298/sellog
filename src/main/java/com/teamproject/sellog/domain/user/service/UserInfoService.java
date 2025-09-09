package com.teamproject.sellog.domain.user.service;

import java.sql.Timestamp;
import java.util.UUID;

import org.springframework.stereotype.Service;
import com.teamproject.sellog.domain.user.model.dto.request.UserProfileRequest;
import com.teamproject.sellog.domain.user.model.dto.response.UserPreviewResponse;
import com.teamproject.sellog.domain.user.model.dto.response.UserProfileResponse;

@Service
public interface UserInfoService {

    UserProfileResponse findUserProfile(String userId, String selfId, String type, Timestamp lastCreateAt,
            UUID lastId, int limit);

    void editUserProfile(String userId, UserProfileRequest userProfileRequest);

    UserPreviewResponse findUserPreview(String userId, String type, Timestamp lastCreateAt, UUID lastId,
            int limit);

}