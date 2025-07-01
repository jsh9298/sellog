package com.teamproject.sellog.domain.user.model.dto.request;

import com.teamproject.sellog.domain.user.model.entity.user.Gender;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public final class UserProfileRequest {
    private final String profileThumbURL;
    private final String profileURL;
    private final String userName;
    private final String nickname;
    private final Gender gender;
    private final String profileMessage;
    private final String birthDay;
    private final String email;
    private final String phoneNumber;
    private final String userAddress;
}
