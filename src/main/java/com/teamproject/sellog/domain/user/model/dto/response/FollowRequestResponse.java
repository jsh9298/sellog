package com.teamproject.sellog.domain.user.model.dto.response;

import com.teamproject.sellog.domain.user.model.entity.friend.FollowRequest;
import lombok.Builder;

import java.sql.Timestamp;
import java.util.UUID;

@Builder
public record FollowRequestResponse(
        UUID requestId,
        UUID requesterId,
        String requesterNickname,
        String requesterProfileThumbURL,
        Timestamp requestedAt) {
    public static FollowRequestResponse fromEntity(FollowRequest followRequest) {
        return FollowRequestResponse.builder()
                .requestId(followRequest.getId())
                .requesterId(followRequest.getRequester().getId())
                .requesterNickname(followRequest.getRequester().getUserProfile().getNickname())
                .requesterProfileThumbURL(followRequest.getRequester().getUserProfile().getProfileThumbURL())
                .requestedAt(followRequest.getCreateAt())
                .build();
    }
}
