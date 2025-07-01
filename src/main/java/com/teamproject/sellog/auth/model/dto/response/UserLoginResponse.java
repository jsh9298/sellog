package com.teamproject.sellog.auth.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public final class UserLoginResponse {
    private final String accessToken;
    private final String refreshToken;
    private final String profileThumbURL;

}