package com.teamproject.sellog.domain.search.service.Impl;

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

import java.util.*;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final SearchIndexRepository searchIndexRepository;
    private final SearchIndexEMRepository searchIndexEMRepository;

    @Transactional(readOnly = true)
    public Map<String, Object> unifiedSearch(UnifiedSearchRequest request, String authenticatedUserId) {
        Map<String, Object> results = new LinkedHashMap<>();
        String fullTextSearchKeyword = "";

        if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
            fullTextSearchKeyword = request.getKeyword() + "*";
        }

        String userId = StringUtils.hasText(authenticatedUserId) ? authenticatedUserId : "";

        if ("ALL".equalsIgnoreCase(request.getTargetType())) {
            // 1. USER 검색 결과를 먼저 가져옴 (페이징 없이 상위 일부만)
            UnifiedSearchRequest userSearchRequest = createSubRequest(request, "USER", 0, 5);
            Pageable userPageable = createPageable(userSearchRequest, "USER");
            List<SearchIndex> userResults = searchIndexEMRepository.findIdsByFullTextSearch(fullTextSearchKeyword,
                    userSearchRequest, userPageable, userId);
            results.put("users", userResults);

            // 2. 나머지 콘텐츠(POST, REVIEW 등) 검색 결과를 가져옴
            UnifiedSearchRequest contentSearchRequest = createSubRequest(request, "CONTENT", request.getPage(),
                    request.getSize());
            Pageable contentPageable = createPageable(contentSearchRequest, "CONTENT");
            List<SearchIndex> contentResults = searchIndexEMRepository.findIdsByFullTextSearch(fullTextSearchKeyword,
                    contentSearchRequest, contentPageable, userId);
            results.put("contents", contentResults);

        } else if ("USER".equalsIgnoreCase(request.getTargetType())) {
            // USER만 검색
            Pageable pageable = createPageable(request, "USER");
            List<SearchIndex> userResults = searchIndexEMRepository.findIdsByFullTextSearch(fullTextSearchKeyword,
                    request, pageable, userId);
            results.put("users", userResults);
        }
        return results;
    }

    private UnifiedSearchRequest createSubRequest(UnifiedSearchRequest original, String targetType, int page,
            int size) {
        UnifiedSearchRequest subRequest = new UnifiedSearchRequest();
        subRequest.setKeyword(original.getKeyword());
        subRequest.setTargetType(targetType);
        subRequest.setSearchOnlyFriends(original.getSearchOnlyFriends());
        subRequest.setSortBy(original.getSortBy());
        subRequest.setPage(page);
        subRequest.setSize(size);
        subRequest.setLatitude(original.getLatitude());
        subRequest.setLongitude(original.getLongitude());
        subRequest.setRadius(original.getRadius());
        subRequest.setMinPrice(original.getMinPrice());
        subRequest.setMaxPrice(original.getMaxPrice());
        return subRequest;
    }

    private Pageable createPageable(UnifiedSearchRequest request, String targetType) {
        Sort sort = getSort(request.getSortBy(), targetType);
        return PageRequest.of(request.getPage(), request.getSize(), sort);
    }

    private Sort getSort(UnifiedSearchRequest.SortBy sortBy, String targetType) {
        if (sortBy == null) {
            sortBy = UnifiedSearchRequest.SortBy.POPULARITY; // 기본값
        }

        switch (sortBy) {
            case LATEST:
                return Sort.by(Sort.Direction.DESC, "createdAt");
            case POPULARITY:
                if ("USER".equalsIgnoreCase(targetType)) {
                    return Sort.by(Sort.Direction.DESC, "followerCount");
                }
                return Sort.by(Sort.Direction.DESC, "likeCount");
            default:
                return Sort.by(Sort.Direction.DESC, "likeCount"); // 기본 정렬
        }
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