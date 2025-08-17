// src/main/java/com/teamproject.sellog.domain.search.repository/SearchIndexRepository.java
package com.teamproject.sellog.domain.search.repository;

import com.teamproject.sellog.domain.search.model.entity.SearchIndex;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface SearchIndexRepository extends JpaRepository<SearchIndex, UUID> {
    Optional<SearchIndex> findBySourceIdAndSourceType(UUID sourceId, String sourceType);

    void deleteBySourceIdAndSourceType(UUID sourceId, String sourceType); // 트랜잭션 내에서 호출

    // Full-Text Search를 활용한 통합 검색 (Querydsl 활용 시 이 부분 대체)
    // MySQL/MariaDB Full-Text Index가 `full_text_content`에 생성되어 있어야 함
    @Query(value = "SELECT si FROM SearchIndex si " +
            "WHERE (:keyword IS NULL OR :keyword = '' OR MATCH(si.fullTextContent) AGAINST(:keyword IN NATURAL LANGUAGE MODE)) "
            +
            "AND (:targetType IS NULL OR si.sourceType = :targetType)", nativeQuery = true) // nativeQuery = true 사용 시
                                                                                            // `si.*` 대신 `si`만 작성
    Page<SearchIndex> unifiedSearch(@Param("keyword") String keyword, @Param("targetType") String targetType,
            Pageable pageable);

    // 친구 목록 내에서 사용자 검색 (sourceType = 'USER'와 authorId가 친구 ID 목록에 속하는 조건 추가)
    @Query(value = "SELECT si FROM SearchIndex si " +
            "WHERE si.sourceType = 'USER' " +
            "AND si.sourceId IN :friendIds " + // User 엔티티의 id는 sourceId에 저장됨. authorId 아님.
            "AND (:keyword IS NULL OR :keyword = '' OR MATCH(si.fullTextContent) AGAINST(:keyword IN NATURAL LANGUAGE MODE))", nativeQuery = true)
    Page<SearchIndex> searchFriends(@Param("keyword") String keyword, @Param("friendIds") Set<UUID> friendIds,
            Pageable pageable);

    // 자동 완성 추천어 조회
    // main_title에 B-Tree 인덱스 필요 (LIKE 검색 최적화)
    @Query(value = "SELECT DISTINCT si.mainTitle FROM SearchIndex si " +
            "WHERE si.mainTitle LIKE :partialKeyword% " + // 접두사 매칭
            "ORDER BY si.mainTitle ASC ", // 정렬
            nativeQuery = true)
    List<String> findAutocompleteSuggestions(@Param("partialKeyword") String partialKeyword, Pageable pageable); // limit는
                                                                                                                 // Pageable로
                                                                                                                 // 처리
}