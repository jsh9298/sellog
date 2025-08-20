package com.teamproject.sellog.domain.search.controller;

import com.teamproject.sellog.domain.search.model.dto.UnifiedSearchRequest;
import com.teamproject.sellog.domain.search.model.entity.SearchIndex;
import com.teamproject.sellog.domain.search.service.SearchService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID; // UUID 사용 예시

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    // 통합 검색 API
    @GetMapping
    public ResponseEntity<Page<SearchIndex>> unifiedSearch(UnifiedSearchRequest dto, HttpServletRequest request) {
        String userId = request.getAttribute("authenticatedUserId").toString();
        Page<SearchIndex> results = searchService.unifiedSearch(dto, userId);
        return ResponseEntity.ok(results);
    }

    // 인기 검색어 추천 API
    @GetMapping("/suggestions/popular")
    public ResponseEntity<List<String>> getPopularSuggestions(@RequestParam(defaultValue = "10") int limit) {
        List<String> suggestions = searchService.getPopularSearchKeywords(limit);
        return ResponseEntity.ok(suggestions);
    }

    // 자동 완성 검색어 추천 API
    @GetMapping("/suggestions/autocomplete")
    public ResponseEntity<List<String>> getAutocompleteSuggestions(@RequestParam String query,
            @RequestParam(defaultValue = "5") int limit) {
        List<String> suggestions = searchService.getAutocompleteSuggestions(query, limit);
        return ResponseEntity.ok(suggestions);
    }
}
