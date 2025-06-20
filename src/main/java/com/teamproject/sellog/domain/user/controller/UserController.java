package com.teamproject.sellog.domain.user.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.teamproject.sellog.common.RestResponse;
import com.teamproject.sellog.domain.user.model.dto.response.UserPrivateDto;
import com.teamproject.sellog.domain.user.model.dto.response.UserProfileDto;
import com.teamproject.sellog.domain.user.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/{userId}/profile")
    public ResponseEntity<?> userProfile(@PathVariable String userId) {
        try {
            UserProfileDto response = userService.findUserProfile(userId);
            return ResponseEntity
                    .ok(new RestResponse<UserProfileDto>(true, "200", "get profile successfully", response));
        } catch (Exception e) {
            return (ResponseEntity<?>) ResponseEntity.badRequest();
        }
    }

    @GetMapping("/{userId}/private")
    public ResponseEntity<?> userPrivate(@PathVariable String userId) {
        try {
            UserPrivateDto response = userService.findUserPrivate(userId);
            return ResponseEntity
                    .ok(new RestResponse<UserPrivateDto>(true, "200", "get privateData successfully", response));
        } catch (Exception e) {
            return (ResponseEntity<?>) ResponseEntity.badRequest();
        }
    }
}
