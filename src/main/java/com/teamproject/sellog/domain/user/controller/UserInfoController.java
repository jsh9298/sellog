package com.teamproject.sellog.domain.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.teamproject.sellog.common.RestResponse;
import com.teamproject.sellog.domain.user.model.dto.request.UserProfileRequest;
import com.teamproject.sellog.domain.user.model.dto.response.UserPreviewResponse;
import com.teamproject.sellog.domain.user.model.dto.response.UserProfileResponse;
import com.teamproject.sellog.domain.user.service.UserInfoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserInfoController {
    private final UserInfoService userInfoService;

    @GetMapping("/{userId}/profile")
    public ResponseEntity<?> userProfile(@PathVariable String userId) {
        try {
            UserProfileResponse response = userInfoService.findUserProfile(userId);
            return ResponseEntity
                    .ok(new RestResponse<UserProfileResponse>(true, "200", "get profileData successfully", response));
        } catch (Exception e) {
            return ResponseEntity.ok(new RestResponse<>(false, "500", e.getMessage(), null));
        }
    }

    @PatchMapping("/{userId}/profile")
    public ResponseEntity<?> userProfileEdit(@PathVariable String userId,
            @RequestBody UserProfileRequest userProfileRequest) {
        try {
            userInfoService.editUserProfile(userId, userProfileRequest);
            UserProfileResponse response = userInfoService.findUserProfile(userId);
            return ResponseEntity
                    .ok(new RestResponse<UserProfileResponse>(true, "200", "edit profileData successfully", response));
        } catch (Exception e) {
            return ResponseEntity.ok(new RestResponse<>(false, "500", e.getMessage(), null));
        }
    }

    @GetMapping("/{userId}/preview")
    public ResponseEntity<?> userProfilePreview(@PathVariable String userId) {
        try {
            UserPreviewResponse response = userInfoService.findUserPreview(userId);
            return ResponseEntity
                    .ok(new RestResponse<UserPreviewResponse>(true, "200", "get previewData successfully", response));
        } catch (Exception e) {
            return ResponseEntity.ok(new RestResponse<>(false, "500", e.getMessage(), null));
        }
    }
}
