package com.teamproject.sellog.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CursorPageResponse<T> {
    private List<T> content; // 현재 페이지의 데이터 목록
    private boolean hasNext; // 다음 페이지가 있는지 여부
    private Timestamp nextCreateAt; // 다음 요청에 사용할 커서 (createAt)
    private UUID nextId; // 다음 요청에 사용할 커서 (id)
}