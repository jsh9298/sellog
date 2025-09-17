package com.teamproject.sellog.domain.search.repository;

import com.teamproject.sellog.domain.search.model.entity.SearchIndex;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Repository
public interface SearchIndexRepository extends JpaRepository<SearchIndex, UUID> {

        // 자동완성 기능
        @Query(value = "SELECT s.main_title FROM search_index s WHERE MATCH(s.full_text_content) AGAINST(:query IN BOOLEAN MODE) LIMIT :limit", nativeQuery = true)
        List<String> findAutocompleteSuggestions(@Param("query") String query, @Param("limit") int limit);

        // sourceId와 sourceType으로 SearchIndex를 조회합니다.
        Optional<SearchIndex> findBySourceIdAndSourceType(UUID sourceId, String sourceType);

        // sourceId와 sourceType으로 SearchIndex를 삭제합니다.
        void deleteBySourceIdAndSourceType(UUID sourceId, String sourceType);

        // @Query(value = "SELECT si.id FROM search_index si " +
        // "WHERE MATCH(si.full_text_content) AGAINST(:searchQuery IN BOOLEAN MODE) >
        // 0.0", nativeQuery = true)
        // List<UUID> findIdsByFullTextSearch(@Param("searchQuery") String searchQuery,
        // Pageable pageable);
}