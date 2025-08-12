package com.teamproject.sellog.common.responseUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public final class CursorPageResponse<T> {
    private final List<T> content; // 현재 페이지의 데이터 목록
    private final boolean hasNext; // 다음 페이지가 있는지 여부
    private final Timestamp nextCreateAt; // 다음 요청에 사용할 커서 (createAt)
    private final UUID nextId; // 다음 요청에 사용할 커서 (id)
}