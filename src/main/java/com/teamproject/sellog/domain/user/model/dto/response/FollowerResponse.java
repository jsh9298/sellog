package com.teamproject.sellog.domain.user.model.dto.response;

import java.sql.Timestamp;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public final class FollowerResponse {
    private final UUID id;
    private final String userId;
    private final String nickname;
    private final String profileThumbURL;
    private final String profileMessage;
    private final Timestamp createAt;
}
