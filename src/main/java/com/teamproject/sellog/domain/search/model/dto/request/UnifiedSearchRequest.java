package com.teamproject.sellog.domain.search.model.dto.request;

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
    private String targetType; // 기능 필터링 ("USER" : 사용자 검색 기능, "ALL" : 일반 검색)
    private Boolean searchOnlyFriends; // 친구만 검색할지 여부 --> target과 조합하여 필터링.HASHTAG 검색에서는 사용x

    // 페이징 및 정렬
    private String sortBy; // 정렬 기준 ("latest":최신순 , "popularity":인기순 , "distance":거리순)--->HASHTAG검색에서는사용x
    private int page = 0;
    private int size = 10;
}