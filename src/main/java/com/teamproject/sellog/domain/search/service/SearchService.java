package com.teamproject.sellog.domain.search.service;

import org.springframework.stereotype.Service;

import com.teamproject.sellog.common.lacationUtils.Location;
import com.teamproject.sellog.common.lacationUtils.UserMapDistance;
import com.teamproject.sellog.domain.post.model.entity.Post;
import com.teamproject.sellog.domain.search.model.dto.UnifiedSearchRequest;
import com.teamproject.sellog.domain.search.model.entity.PopularSearchKeyword;
import com.teamproject.sellog.domain.search.model.entity.SearchIndex;
import com.teamproject.sellog.domain.search.repository.PopularSearchKeywordRepository;
import com.teamproject.sellog.domain.search.repository.SearchIndexRepository;
import com.teamproject.sellog.domain.user.model.entity.user.User;
import com.teamproject.sellog.domain.user.repository.UserRepository;
// import com.teamproject.sellog.domain.user.repository.FriendshipRepository; // 친구 목록 조회 Repository (가정)

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 트랜잭션 필요 시
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j // 로깅을 위한 Lombok 어노테이션
@Service
@RequiredArgsConstructor
public class SearchService {

    private final SearchIndexRepository searchIndexRepository;
    private final PopularSearchKeywordRepository popularSearchKeywordRepository;
    private final UserRepository userRepository;
    // private final FriendshipRepository friendshipRepository; // Friendship 엔티티와
    // Repository가 있다고 가정

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
            User currentUser = userRepository.findById(authenticatedUserId)
                    .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found."));
            Set<UUID> friendIds = friendshipRepository.findFriendIdsByUserId(currentUser.getId()); // 친구 ID 목록 조회

            if (friendIds.isEmpty()) {
                return Page.empty(pageable); // 친구가 없으면 빈 페이지 반환
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
            // 현재 코드에는 해당 쿼리가 없으므로, 모든 결과를 가져와 서비스에서 필터링 (비효율적이지만 예시)
            Page<SearchIndex> resultsInBoundingBox = searchIndexRepository.unifiedSearch(keyword, targetType, pageable);

            // 2차 필터링: 정확한 거리 계산
            List<SearchIndex> filteredByDistance = resultsInBoundingBox.getContent().stream()
                    .filter(searchItem -> {
                        // SearchIndex에 locationPoint가 있다고 가정
                        if (searchItem.getLocationPoint() == null) {
                            return false; // 위치 정보 없는 항목 제외
                        }
                        Location itemLocation = new Location(searchItem.getLocationPoint().getY(),
                                searchItem.getLocationPoint().getX()); // JTS Point (X,Y) -> Location (Lat,Lon)
                        double distance = UserMapDistance.calculateDistance(userLocation,
                                new Post(null, null, itemLocation)); // Post 객체 생성하여 전달 (Post.getLocation() 호출을 위함)
                        return distance <= (searchRadiusKm * 1000); // km -> m 변환
                    })
                    .collect(Collectors.toList());

            // 페이지 재구성
            return new org.springframework.data.domain.PageImpl<>(filteredByDistance, pageable,
                    filteredByDistance.size());
        }

        // 3. 일반적인 통합 검색 (키워드 및 대상 타입 필터링)
        return searchIndexRepository.unifiedSearch(keyword, targetType, pageable);
    }

    // --- 검색어 추천 기능 ---

    // 1. 검색어 로깅/카운트 업데이트 (게시글 검색 후 호출될 수 있음)
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
        // limit에 맞는 Pageable 객체 생성
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
        Pageable pageable = PageRequest.of(0, limit); // 정렬은 쿼리에서
        return searchIndexRepository.findAutocompleteSuggestions(partialQuery.trim(), pageable);
    }
}