package com.teamproject.sellog.domain.search.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.teamproject.sellog.domain.search.model.dto.request.UnifiedSearchRequest;

@Service
public interface SearchService {
    Map<String, Object> unifiedSearch(UnifiedSearchRequest request, String authenticatedUserId);

    List<String> getAutocompleteSuggestions(String partialQuery, int limit);
}
