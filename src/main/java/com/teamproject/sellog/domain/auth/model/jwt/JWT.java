package com.teamproject.sellog.domain.auth.model.jwt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public final class JWT {
    private final String accessToken;
    private final String refreshToken;

}