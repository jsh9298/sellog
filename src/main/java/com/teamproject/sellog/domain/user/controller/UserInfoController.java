package com.teamproject.sellog.domain.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.teamproject.sellog.common.accountsUtils.CheckStatus;
import com.teamproject.sellog.common.dtoUtils.RestResponse;
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
        try {
            UserProfileResponse response = userInfoService.findUserProfile(userId, selfId);
            return ResponseEntity
                    .ok(new RestResponse<UserProfileResponse>(true, "200", "get profileData successfully", response));
        } catch (Exception e) {
            return ResponseEntity.ok(new RestResponse<>(false, "500", e.getMessage(), null));
        }
    }

    @PatchMapping("/profile")
    @Operation(summary = "본인 프로필 수정(회원용)", description = "프로필(개인정보 포함) 수정(+)")
    public ResponseEntity<?> userProfileEdit(@RequestBody UserProfileRequest userProfileRequest,
            HttpServletRequest request) {
        try {
            String selfId = request.getAttribute("authenticatedUserId").toString();
            if (CheckStatus.checkSelf(selfId, selfId)) {
                userInfoService.editUserProfile(selfId, userProfileRequest);

                UserProfileResponse response = userInfoService.findUserProfile(selfId, selfId);
                return ResponseEntity
                        .ok(new RestResponse<UserProfileResponse>(true, "200", "edit profileData successfully",
                                response));
            } else {
                return ResponseEntity
                        .ok(new RestResponse<>(true, "400", "Bad Request", null));
            }
        } catch (Exception e) {
            return ResponseEntity.ok(new RestResponse<>(false, "500", e.getMessage(), null));
        }
    }

    @GetMapping("/preview/{userId}")
    @Operation(summary = "프로필 조회(비회원용)", description = "프로필(개인정보 미포함) 출력(+)")
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
