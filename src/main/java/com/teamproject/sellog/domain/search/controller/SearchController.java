package com.teamproject.sellog.domain.search.controller;

import com.teamproject.sellog.domain.search.model.dto.request.UnifiedSearchRequest;
import com.teamproject.sellog.domain.search.model.entity.SearchIndex;
import com.teamproject.sellog.domain.search.service.SearchService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import org.springdoc.core.converters.models.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    // 통합 검색 API
    @GetMapping
    @Operation(summary = "검색(+)", description = "일단 열어는 둠")
    public ResponseEntity<Map<String, Object>> unifiedSearch(UnifiedSearchRequest dto, HttpServletRequest request) {
        String userId = request.getAttribute("authenticatedUserId").toString();
        Map<String, Object> results = searchService.unifiedSearch(dto, userId);
        return ResponseEntity.ok(results); // USER와 CONTENTS를 포함한 Map 반환
    }

    // 자동 완성 검색어 추천 API
    @GetMapping("/suggestions/autocomplete")
    @Operation(summary = "검색어 추천(+)", description = "일단 열어는 둠")
    public ResponseEntity<?> getAutocompleteSuggestions(@RequestParam String query,
            @RequestParam(defaultValue = "5") int limit) {
        List<String> suggestions = searchService.getAutocompleteSuggestions(query, limit);
        return ResponseEntity.ok(suggestions);
    }
}