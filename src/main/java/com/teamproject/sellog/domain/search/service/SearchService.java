package com.teamproject.sellog.domain.search.service;

import com.teamproject.sellog.common.locationUtils.Location;
import com.teamproject.sellog.common.locationUtils.UserMapDistance;
import com.teamproject.sellog.domain.post.model.entity.Post;
import com.teamproject.sellog.domain.search.model.dto.UnifiedSearchRequest;
import com.teamproject.sellog.domain.search.model.entity.PopularSearchKeyword;
import com.teamproject.sellog.domain.search.model.entity.SearchIndex;
import com.teamproject.sellog.domain.search.repository.PopularSearchKeywordRepository;
import com.teamproject.sellog.domain.search.repository.SearchIndexRepository;
import com.teamproject.sellog.domain.user.model.entity.user.User;
import com.teamproject.sellog.domain.user.repository.UserRepository;

import com.teamproject.sellog.domain.user.repository.FollowRepository; // FollowRepository 추가
import com.teamproject.sellog.domain.user.repository.BlockRepository; // BlockRepository 추가

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final SearchIndexRepository searchIndexRepository;
    private final PopularSearchKeywordRepository popularSearchKeywordRepository;
    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final BlockRepository blockRepository;

    // --- 통합 검색 로직 ---
    @Transactional(readOnly = true)
    public Page<SearchIndex> unifiedSearch(UnifiedSearchRequest request, UUID authenticatedUserId) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), Sort.by("createdAt").descending()); // 예시
                                                                                                                     // 정렬

        String keyword = request.getKeyword();
        String targetType = request.getTargetType();

        // 1. 친구 검색 필터링 (searchOnlyFriends && targetType = "USER")
        if (request.getSearchOnlyFriends() != null && request.getSearchOnlyFriends()
                && "USER".equalsIgnoreCase(targetType)) {
            if (authenticatedUserId == null) {
                // 인증된 사용자 ID가 없으면 친구 목록을 조회할 수 없음
                log.warn("Attempt to search only friends without authenticated user ID.");
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
                log.info("No friends found or all followed users are blocked for user: {}", authenticatedUserId);
                return Page.empty(pageable); // 친구가 없거나 모두 차단했다면 빈 페이지 반환
            }
            return searchIndexRepository.searchFriends(keyword, friendIds, pageable);
        }

        // 2. 위치 기반 검색 (latitude, longitude, searchRadiusKm 존재 시)
        if (request.getLatitude() != null && request.getLongitude() != null && request.getSearchRadiusKm() != null) {
            Location userLocation = new Location(request.getLatitude(), request.getLongitude());
            double searchRadiusKm = request.getSearchRadiusKm();

            // 경계 상자 계산
            Location northeast = UserMapDistance.aroundCustomerNortheastDot(userLocation, searchRadiusKm);
            Location southwest = UserMapDistance.aroundCustomerSouthwestDot(userLocation, searchRadiusKm);

            // TODO: searchIndexRepository에 Bounding Box 기반 검색 쿼리 추가 필요
            // 이 쿼리는 SearchIndex.locationPoint를 사용하여 DB에서 공간 필터링을 수행해야 합니다.
            // 아래는 예시를 위한 임시 코드 (실제 서비스에서는 최적화 필요)
            Page<SearchIndex> resultsInBoundingBox = searchIndexRepository.unifiedSearch(keyword, targetType, pageable);

            // 2차 필터링: 정확한 거리 계산 (서비스 단에서 필터링 - 효율성 저하)
            List<SearchIndex> filteredByDistance = resultsInBoundingBox.getContent().stream()
                    .filter(searchItem -> {
                        // SearchIndex에 locationPoint가 있다고 가정
                        if (searchItem.getLocationPoint() == null) {
                            return false; // 위치 정보 없는 항목 제외
                        }
                        Location itemLocation = new Location(searchItem.getLocationPoint().getY(),
                                searchItem.getLocationPoint().getX()); // JTS Point (X,Y) -> Location (Lat,Lon)
                        // calculateDistance는 Post 엔티티를 받도록 되어 있어, 여기서는 Post mock 또는 Post 대신 location만
                        // 받는 오버로드 메서드 필요
                        // 간단한 Post 객체 생성 (실제 사용은 추천하지 않음, calculateDistance에 location만 받는 오버로드 만드는 것을
                        // 권장)
                        Post tempPost = new Post(null, null, itemLocation);
                        double distance = UserMapDistance.calculateDistance(userLocation, tempPost); // Post 객체 생성하여 전달
                                                                                                     // (Post.getLocation()
                                                                                                     // 호출을 위함)
                        return distance <= (searchRadiusKm * 1000); // km -> m 변환
                    })
                    .collect(Collectors.toList());

            // 페이지 재구성
            return new org.springframework.data.domain.PageImpl<>(filteredByDistance, pageable,
                    resultsInBoundingBox.getTotalElements());
        }

        // 3. 일반적인 통합 검색 (키워드 및 대상 타입 필터링)
        return searchIndexRepository.unifiedSearch(keyword, targetType, pageable);
    }

    // --- 검색어 추천 기능 ---

    // 1. 검색어 로깅/카운트 업데이트
    @Transactional
    public void logSearchKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty())
            return;

        Optional<PopularSearchKeyword> existingKeyword = popularSearchKeywordRepository.findById(keyword);
        if (existingKeyword.isPresent()) {
            PopularSearchKeyword pk = existingKeyword.get();
            pk.setSearchCount(pk.getSearchCount() + 1);
            pk.setLastUpdatedAt(LocalDateTime.now());
            popularSearchKeywordRepository.save(pk);
            log.info("Updated popular keyword: {}", keyword);
        } else {
            popularSearchKeywordRepository.save(new PopularSearchKeyword(keyword, 1L, LocalDateTime.now()));
            log.info("New popular keyword logged: {}", keyword);
        }
    }

    // 2. 인기 검색어 조회
    @Transactional(readOnly = true)
    public List<String> getPopularSearchKeywords(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by("searchCount").descending());
        return popularSearchKeywordRepository.findTopByOrderBySearchCountDesc(pageable)
                .stream()
                .map(PopularSearchKeyword::getKeyword)
                .collect(Collectors.toList());
    }

    // 3. 자동 완성 추천어 조회
    @Transactional(readOnly = true)
    public List<String> getAutocompleteSuggestions(String partialQuery, int limit) {
        if (partialQuery == null || partialQuery.trim().isEmpty()) {
            return Collections.emptyList();
        }
        Pageable pageable = PageRequest.of(0, limit);
        return searchIndexRepository.findAutocompleteSuggestions(partialQuery.trim(), pageable);
    }
}