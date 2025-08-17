package com.teamproject.sellog.domain.search.controller;

import com.teamproject.sellog.domain.search.model.dto.UnifiedSearchRequest;
import com.teamproject.sellog.domain.search.model.entity.SearchIndex;
import com.teamproject.sellog.domain.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID; // UUID 사용 예시

@Slf4j
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    // 통합 검색 API
    @GetMapping
    public ResponseEntity<Page<SearchIndex>> unifiedSearch(UnifiedSearchRequest request) {
        // 실제 애플리케이션에서는 Spring Security ContextHolder 등에서 인증된 사용자 ID를 가져와야 합니다.
        // 여기서는 예시를 위해 request.getCurrentUserId()를 사용
        UUID authenticatedUserId = request.getCurrentUserId() != null ? UUID.fromString(request.getCurrentUserId())
                : null;

        log.info("Unified search request: {}", request);
        Page<SearchIndex> results = searchService.unifiedSearch(request, authenticatedUserId);
        return ResponseEntity.ok(results);
    }

    // 인기 검색어 추천 API
    @GetMapping("/suggestions/popular")
    public ResponseEntity<List<String>> getPopularSuggestions(@RequestParam(defaultValue = "10") int limit) {
        List<String> suggestions = searchService.getPopularSearchKeywords(limit);
        log.info("Popular search suggestions requested, limit: {}, results: {}", limit, suggestions.size());
        return ResponseEntity.ok(suggestions);
    }

    // 자동 완성 검색어 추천 API
    @GetMapping("/suggestions/autocomplete")
    public ResponseEntity<List<String>> getAutocompleteSuggestions(@RequestParam String query,
            @RequestParam(defaultValue = "5") int limit) {
        List<String> suggestions = searchService.getAutocompleteSuggestions(query, limit);
        log.info("Autocomplete suggestions requested for query: '{}', limit: {}, results: {}", query, limit,
                suggestions.size());
        return ResponseEntity.ok(suggestions);
    }
}
