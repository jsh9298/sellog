package com.teamproject.sellog.domain.user.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public final class OtherUserIdRequest {
    private final String UserId;

}
