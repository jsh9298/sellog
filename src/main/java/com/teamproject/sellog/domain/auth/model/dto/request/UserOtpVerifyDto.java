package com.teamproject.sellog.domain.auth.model.dto.request;

import lombok.Getter;

@Getter
public class UserOtpVerifyDto {
    private String userId;
    private String otp;
}