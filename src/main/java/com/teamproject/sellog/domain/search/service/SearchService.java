package com.teamproject.sellog.domain.search.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.teamproject.sellog.domain.search.model.dto.UnifiedSearchRequest;
import com.teamproject.sellog.domain.search.model.entity.SearchIndex;

@Service
public interface SearchService {
    Page<SearchIndex> unifiedSearch(UnifiedSearchRequest request, String authenticatedUserId);

    List<String> getAutocompleteSuggestions(String partialQuery, int limit);
}
