package com.teamproject.sellog.domain.search.service.Impl;

import com.teamproject.sellog.common.locationUtils.Location;
import com.teamproject.sellog.domain.search.model.dto.request.UnifiedSearchRequest;
import com.teamproject.sellog.domain.search.model.entity.SearchIndex;
import com.teamproject.sellog.domain.search.repository.SearchIndexEMRepository;
import com.teamproject.sellog.domain.search.repository.SearchIndexRepository;
import com.teamproject.sellog.domain.search.service.SearchService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final SearchIndexRepository searchIndexRepository;
    private final SearchIndexEMRepository searchIndexEMRepository;

    // --- 통합 검색 로직 ---
    @Transactional(readOnly = true)
    public List<SearchIndex> unifiedSearch(UnifiedSearchRequest request, String authenticatedUserId) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(),
                Sort.by(request.getSortBy()));
        String fullTextSearchKeyword = "";
        List<SearchIndex> matchedSearchIndexIds = new ArrayList<>();

        if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
            fullTextSearchKeyword = request.getKeyword() + "*";
        }
        if (!StringUtils.hasText(authenticatedUserId)) {
            matchedSearchIndexIds = searchIndexEMRepository.findIdsByFullTextSearch(fullTextSearchKeyword,
                    request, pageable, "");
        } else {
            matchedSearchIndexIds = searchIndexEMRepository.findIdsByFullTextSearch(fullTextSearchKeyword,
                    request, pageable, authenticatedUserId);
        }

        return matchedSearchIndexIds;

    }

    // 자동완성
    @Transactional(readOnly = true)
    public List<String> getAutocompleteSuggestions(String partialQuery, int limit) {
        if (partialQuery == null || partialQuery.trim().isEmpty()) {
            return Collections.emptyList();
        }
        // 네이티브 쿼리의 boolean mode에서는 prefix 검색을 위해 `*`를 붙여줍니다.
        return searchIndexRepository.findAutocompleteSuggestions(partialQuery.trim() + "*", limit);
    }

}