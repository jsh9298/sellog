package com.teamproject.sellog.domain.search.model.dto;

// src/main/java/com/teamproject/sellog.service.dto/UnifiedSearchRequest.java

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UnifiedSearchRequest {
    private String keyword; // 검색 키워드 (부분 검색어)
    private String currentUserId; // 현재 검색을 수행하는 사용자 ID (인증 정보에서 가져옴)
    private String targetType; // 검색 대상 필터링 (예: "POST", "USER", "COMMENT", "HASHTAG", "ALL")
    private Boolean searchOnlyFriends; // 친구만 검색할지 여부

    // 위치 기반 검색 관련
    private Double latitude;
    private Double longitude;
    private Double searchRadiusKm;

    // 페이징 및 정렬
    private String sortBy; // 정렬 기준 (예: "latest", "popularity", "distance")
    private int page = 0;
    private int size = 10;
}