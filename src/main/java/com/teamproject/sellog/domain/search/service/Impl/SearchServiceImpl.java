package com.teamproject.sellog.domain.search.service.Impl;

import com.teamproject.sellog.common.locationUtils.Location;
import com.teamproject.sellog.domain.search.model.dto.UnifiedSearchRequest;
import com.teamproject.sellog.domain.search.model.entity.SearchIndex;

import com.teamproject.sellog.domain.search.repository.SearchIndexRepository;
import com.teamproject.sellog.domain.search.service.SearchService;
import com.teamproject.sellog.domain.user.repository.FollowRepository; // FollowRepository 추가
import com.teamproject.sellog.domain.user.repository.BlockRepository; // BlockRepository 추가

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final SearchIndexRepository searchIndexRepository;
    private final FollowRepository followRepository;
    private final BlockRepository blockRepository;

    // --- 통합 검색 로직 ---
    @Transactional(readOnly = true)
    public Page<SearchIndex> unifiedSearch(UnifiedSearchRequest request, String authenticatedUserId) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(),
                Sort.by(Sort.Direction.DESC, "createdAt"));

        // Specification을 사용하여 동적 쿼리 생성
        return searchIndexRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 키워드 검색 (MySQL Full-Text Search)
            if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
                String fullTextSearchKeyword = request.getKeyword() + "*";
                List<UUID> matchedSearchIndexIds = searchIndexRepository
                        .findIdsByFullTextSearch(fullTextSearchKeyword);

                if (!matchedSearchIndexIds.isEmpty()) {
                    predicates.add(root.get("id").in(matchedSearchIndexIds));
                } else {
                    predicates.add(cb.isFalse(cb.literal(true)));
                }

            }

            // 검색 대상 타입 필터링 (POST, USER)
            if (request.getTargetType() != null && !request.getTargetType().isBlank()) {
                predicates.add(cb.equal(root.get("sourceType"), request.getTargetType()));
            }

            // 친구만 검색 필터링 (USER 검색 시)
            if (request.getSearchOnlyFriends() != null && request.getSearchOnlyFriends()
                    && "USER".equalsIgnoreCase(request.getTargetType())) {
                if (authenticatedUserId != null) {
                    List<UUID> friendIds = getFriendIds(authenticatedUserId);
                    if (friendIds.isEmpty()) {
                        // 친구가 없으면 결과가 없도록 항상 false인 조건을 추가
                        predicates.add(cb.isFalse(cb.literal(true)));
                    } else {
                        predicates.add(root.get("sourceId").in(friendIds));
                    }
                } else {
                    // 로그인하지 않은 사용자가 친구 검색을 요청하면 결과 없음
                    predicates.add(cb.isFalse(cb.literal(true)));
                }
            }

            // TODO: 향후 위치 기반 검색 필터 추가, 근데 그전에 sql 파일 깨진거부터 교체를 해야한다는 슬픈 사실..

            return cb.and(predicates.toArray(new Predicate[0]));
        }, pageable);
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

    private List<UUID> getFriendIds(String userId) {
        List<UUID> followingIds = followRepository.findFollowingIdsByFollowerId(userId);
        List<UUID> blockedIds = blockRepository.findBlockedIdsByBlockerId(userId);
        followingIds.removeAll(blockedIds);
        return followingIds;
    }
}