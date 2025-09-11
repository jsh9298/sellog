package com.teamproject.sellog.domain.auth.model.dto.request;

import lombok.Getter;

@Getter
public class UserOtpRequestDto {
    private String userId;
    private String email;
}