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
    /*
     * [중요] Full-Text 검색을 위한 데이터베이스 사전 설정
     *
     * 이 검색 기능은 MariaDB/MySQL의 Full-Text Search를 사용합니다.
     * 한글 검색이 올바르게 동작하려면 데이터베이스에 N-gram 파서 설정이 반드시 필요합니다.
     *
     * 1. MariaDB/MySQL 설정 파일(my.cnf 또는 my.ini)에 다음 설정을 추가하고 DB를 재시작해야 합니다.
     * [mysqld]
     * ngram_token_size=2
     *
     * 2. `search_index` 테이블의 `full_text_content` 컬럼에 N-gram 파서를 사용하는 FULLTEXT 인덱스를
     * 생성해야 합니다.
     * ALTER TABLE search_index ADD FULLTEXT INDEX ft_content_idx
     * (full_text_content) WITH PARSER ngram;
     *
     * 3. 위 설정이 없으면 `findIdsByFullTextSearch` 쿼리가 항상 빈 결과를 반환하여,
     * 키워드 검색 시 결과가 나오지 않을 수 있습니다.
     */
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
                    // 키워드 검색 결과가 없으면, 다른 조건(타입, 친구 등)으로만 검색되도록
                    // 항상 false인 조건을 추가하지 않고 넘어갑니다.
                    // 이렇게 하면 키워드와 일치하는 내용이 없어도 친구 목록이나 특정 타입의 게시물 전체를 볼 수 있습니다.
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