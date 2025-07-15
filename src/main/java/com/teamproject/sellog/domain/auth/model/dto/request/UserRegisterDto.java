package com.teamproject.sellog.domain.auth.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public final class UserRegisterDto {
    private final String name;
    private final String nickname;
    private final String userId;
    private final String password;
    private final String email;
}