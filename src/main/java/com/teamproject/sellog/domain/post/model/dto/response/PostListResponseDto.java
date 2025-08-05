package com.teamproject.sellog.domain.post.model.dto.response;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.UUID;

import com.teamproject.sellog.domain.post.model.entity.PostType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class PostListResponseDto {
    private final UUID id;
    private final PostType type;
    private final String title;
    private final String nickname;
    private final BigInteger price;
    private final String place;
    private final Timestamp createAt;
    private final Timestamp updateAt;
    private final BigInteger likeCnt;
    private final BigInteger readCnt;
    private final String thumbnail;
}
