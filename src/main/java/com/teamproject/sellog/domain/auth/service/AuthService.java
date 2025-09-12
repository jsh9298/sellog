package com.teamproject.sellog.domain.auth.service;

import com.teamproject.sellog.domain.auth.model.dto.request.UserOtpRequestDto;
import com.teamproject.sellog.domain.auth.model.dto.request.UserOtpVerifyDto;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.teamproject.sellog.domain.auth.model.dto.request.UserLoginDto;
import com.teamproject.sellog.domain.auth.model.dto.request.UserRegisterDto;
import com.teamproject.sellog.domain.auth.model.dto.response.UserLoginResponse;
import com.teamproject.sellog.domain.auth.model.jwt.JWT;
import com.teamproject.sellog.domain.user.model.entity.user.User;

@Service
public interface AuthService {
    User registerUser(UserRegisterDto userRegisterDto);

    UserLoginResponse loginUser(UserLoginDto userLoginDto);

    void logoutUser(String accessToken, String refreshToken);

    void deleteUser(String userId, String password, String accessToken, String refreshToken);

    JWT refreshToken(String accessToken, String refreshToken);

    String findUserId(String email);

    void sendOtpForPasswordReset(UserOtpRequestDto dto);

    boolean verifyOtp(UserOtpVerifyDto dto);

    void changePassword(String userId, String password);

    boolean checkId(String userId);

    boolean isAccessTokenBlacklisted(String token);

    Optional<User> findByUserIdWithDetails(String userId);
}
