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
public class SimplePostList {
    private final UUID postId;
    private final String thumbnail;
    private final Timestamp createAt;
}
