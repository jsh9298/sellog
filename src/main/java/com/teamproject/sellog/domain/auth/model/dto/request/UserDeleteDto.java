package com.teamproject.sellog.domain.auth.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public final class UserDeleteDto {

    private final String userId;
    private final String password;
}
