package com.teamproject.sellog.domain.search.repository;

import com.teamproject.sellog.domain.search.model.entity.PopularSearchKeyword;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PopularSearchKeywordRepository extends JpaRepository<PopularSearchKeyword, String> {
    // 검색 횟수 기준으로 내림차순 정렬된 상위 N개 키워드 조회
    List<PopularSearchKeyword> findTopByOrderBySearchCountDesc(Pageable pageable); // Pageable로 limit 처리
}