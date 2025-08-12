package com.teamproject.sellog.domain.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.teamproject.sellog.common.responseUtils.RestResponse;
import com.teamproject.sellog.domain.user.model.dto.request.UserProfileRequest;
import com.teamproject.sellog.domain.user.model.dto.response.UserPreviewResponse;
import com.teamproject.sellog.domain.user.model.dto.response.UserProfileResponse;
import com.teamproject.sellog.domain.user.service.UserInfoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "사용자 프로필", description = "프로필/개인정보 조회 관련 API")
public class UserInfoController {
    private final UserInfoService userInfoService;

    @GetMapping("/profile/{userId}")
    @Operation(summary = "상대 프로필 조회(회원용)", description = "프로필(개인정보 포함) 출력(+)")
    public ResponseEntity<?> userProfile(@PathVariable String userId, HttpServletRequest request) {
        String selfId = request.getAttribute("authenticatedUserId").toString();

        UserProfileResponse response = userInfoService.findUserProfile(userId, selfId);
        return ResponseEntity
                .ok(RestResponse.success("get profileData successfully", response));

    }

    @PatchMapping("/profile")
    @Operation(summary = "본인 프로필 수정(회원용)", description = "프로필(개인정보 포함) 수정(+)")
    public ResponseEntity<?> userProfileEdit(@RequestBody UserProfileRequest userProfileRequest,
            HttpServletRequest request) {

        String selfId = request.getAttribute("authenticatedUserId").toString();

        userInfoService.editUserProfile(selfId, userProfileRequest);

        UserProfileResponse response = userInfoService.findUserProfile(selfId, selfId);
        return ResponseEntity
                .ok(RestResponse.success("edit profileData successfully", response));

    }

    @GetMapping("/preview/{userId}")
    @Operation(summary = "프로필 조회(비회원용)", description = "프로필(개인정보 미포함) 출력(+)")
    public ResponseEntity<?> userProfilePreview(@PathVariable String userId) {

        UserPreviewResponse response = userInfoService.findUserPreview(userId);
        return ResponseEntity
                .ok(RestResponse.success("get previewData successfully", response));

    }
}
