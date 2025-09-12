package com.teamproject.sellog.domain.auth.model.jwt;

import com.fasterxml.jackson.annotation.JsonInclude;
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
    @JsonInclude(JsonInclude.Include.NON_NULL) // refreshToken이 null일 경우 JSON 응답에 포함하지 않음
    private final String refreshToken;

}