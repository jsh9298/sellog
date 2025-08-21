package com.teamproject.sellog.domain.search.service;

import com.teamproject.sellog.domain.search.model.dto.UnifiedSearchRequest;
import com.teamproject.sellog.domain.search.model.entity.SearchIndex;

import com.teamproject.sellog.domain.search.repository.SearchIndexRepository;
import com.teamproject.sellog.domain.user.repository.UserRepository;

import com.teamproject.sellog.domain.user.repository.FollowRepository; // FollowRepository 추가
import com.teamproject.sellog.domain.user.repository.BlockRepository; // BlockRepository 추가

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final SearchIndexRepository searchIndexRepository;
    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final BlockRepository blockRepository;

    // --- 통합 검색 로직 ---
    @Transactional(readOnly = true)
    public Page<SearchIndex> unifiedSearch(UnifiedSearchRequest request, String authenticatedUserId) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), Sort.by("createdAt").descending()); // 예시
                                                                                                                     // 정렬

        String keyword = request.getKeyword();
        String targetType = request.getTargetType();

        // 1. 친구 검색 필터링 (searchOnlyFriends && targetType = "USER")
        if (request.getSearchOnlyFriends() != null && request.getSearchOnlyFriends()
                && "USER".equalsIgnoreCase(targetType)) {
            if (authenticatedUserId == null) {
                // 인증된 사용자 ID가 없으면 친구 목록을 조회할 수 없음
                return Page.empty(pageable);
            }

            // (1) 현재 사용자가 팔로우하는 모든 사용자 ID 조회
            Set<UUID> followingIds = followRepository.findFollowingIdsByFollowerId(authenticatedUserId)
                    .stream()
                    .collect(Collectors.toSet());

            // (2) 현재 사용자가 차단한 모든 사용자 ID 조회
            Set<UUID> blockedIds = blockRepository.findBlockedIdsByBlockerId(authenticatedUserId)
                    .stream()
                    .collect(Collectors.toSet());

            // (3) 팔로우하는 사용자 중 차단하지 않은 사용자만 필터링 = 실제 '친구' 목록
            Set<UUID> friendIds = followingIds.stream()
                    .filter(id -> !blockedIds.contains(id))
                    .collect(Collectors.toSet());

            if (friendIds.isEmpty()) {
                return Page.empty(pageable); // 친구가 없거나 모두 차단했다면 빈 페이지 반환
            }
            return searchIndexRepository.searchFriends(keyword, friendIds, pageable);
        }

        // 사용자 검색
        if (request.getSearchOnlyFriends() != null && !request.getSearchOnlyFriends()
                && "USER".equalsIgnoreCase(targetType)) {
            return searchIndexRepository.searchUser(keyword, pageable);
        }

        // 게시글 검색
        if (request.getSearchOnlyFriends() == null && "POST".equalsIgnoreCase(targetType)) {
            return searchIndexRepository.searchPost(keyword, pageable);
        }

        // 3. 일반적인 통합 검색 (키워드 및 대상 타입 필터링)
        return searchIndexRepository.unifiedSearch(keyword, targetType, pageable);
    }

    // 자동완성
    @Transactional(readOnly = true)
    public List<String> getAutocompleteSuggestions(String partialQuery, int limit) {
        if (partialQuery == null || partialQuery.trim().isEmpty()) {
            return Collections.emptyList();
        }
        Pageable pageable = PageRequest.of(0, limit);
        return searchIndexRepository.findAutocompleteSuggestions(partialQuery.trim(), pageable);
    }

}