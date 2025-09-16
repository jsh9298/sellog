package com.teamproject.sellog.domain.search.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.teamproject.sellog.domain.search.model.dto.request.UnifiedSearchRequest;
import com.teamproject.sellog.domain.search.model.entity.SearchIndex;

@Service
public interface SearchService {
    List<SearchIndex> unifiedSearch(UnifiedSearchRequest request, String authenticatedUserId);

    List<String> getAutocompleteSuggestions(String partialQuery, int limit);
}
