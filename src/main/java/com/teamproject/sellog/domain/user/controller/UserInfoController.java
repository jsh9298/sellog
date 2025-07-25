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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "사용자 프로필", description = "프로필/개인정보 조회 관련 API")
public class UserInfoController {
    private final UserInfoService userInfoService;

    @GetMapping("/{userId}/profile")
    @Operation(summary = "프로필 조회(회원용)", description = "프로필(개인정보 포함) 출력(*)")
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
    @Operation(summary = "프로필 수정(회원용)", description = "프로필(개인정보 포함) 수정(*)")
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
    @Operation(summary = "프로필 조회(비회원용)", description = "프로필(개인정보 미포함) 출력(*)")
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
