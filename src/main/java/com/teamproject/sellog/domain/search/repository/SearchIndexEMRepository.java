package com.teamproject.sellog.domain.search.repository;

import com.teamproject.sellog.domain.search.model.dto.UnifiedSearchRequest;
import com.teamproject.sellog.domain.search.model.entity.SearchIndex;
import com.teamproject.sellog.domain.user.repository.BlockRepository;
import com.teamproject.sellog.domain.user.repository.FollowRepository;
import java.util.ArrayList;

import java.util.List;
import java.util.UUID;

import org.springframework.core.annotation.MergedAnnotations.Search;
import org.springframework.data.domain.Pageable;

import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import jakarta.persistence.Query;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Repository
public class SearchIndexEMRepository {
    @PersistenceContext
    private EntityManager entityManager;

    private final FollowRepository followRepository;
    private final BlockRepository blockRepository;

    public SearchIndexEMRepository(FollowRepository followRepository, BlockRepository blockRepository) {
        this.followRepository = followRepository;
        this.blockRepository = blockRepository;
    }

    public List<SearchIndex> findIdsByFullTextSearch(String searchQuery, UnifiedSearchRequest request,
            Pageable pageable,
            String userId) {
        // JPQL 대신 Native SQL Query를 사용합니다.
        StringBuilder sql = new StringBuilder("SELECT * FROM search_index si WHERE 1=1");
        List<Object> parameters = new ArrayList<>();

        if (StringUtils.hasText(request.getTargetType())) {
            sql.append(" AND si.source_type = ?"); // 실제 컬럼명 사용
            parameters.add(request.getTargetType());

            if (request.getTargetType().equals("USER")
                    && request.getSearchOnlyFriends() != null
                    && request.getSearchOnlyFriends()
                    && StringUtils.hasText(userId)) { // 친구찾기활성및userId를받을경우
                List<UUID> followingIds = getFriendIds(userId);
                if (followingIds.isEmpty()) {
                    // 친구가 없으면 결과가 없어야 하므로 항상 false인 조건을 추가합니다.
                    sql.append(" AND 1=0");
                } else {
                    // IN 절에 대한 파라미터 바인딩을 위해 ? 개수를 동적으로 생성합니다.
                    sql.append(" AND si.source_id IN (");
                    for (int i = 0; i < followingIds.size(); i++) {
                        sql.append(i == 0 ? "?" : ", ?");
                    }
                    sql.append(")");
                    parameters.addAll(followingIds);
                }
            }
        }

        // 검색어가 있을 경우에만 MATCH AGAINST 절을 추가합니다.
        if (StringUtils.hasText(searchQuery)) {
            // MariaDB의 Full-Text Search 네이티브 함수를 사용합니다.
            sql.append(" AND MATCH(si.full_text_content) AGAINST(? IN BOOLEAN MODE)");
            parameters.add(searchQuery);
        }

        // 정렬 조건 추가
        if (pageable.getSort().isSorted()) {
            sql.append(" ORDER BY ");
            pageable.getSort().forEach(order -> {
                // JPQL 필드 이름(authorNickname)을 실제 DB 컬럼명(author_nickname)으로 변환해야 합니다.
                // 여기서는 간단한 예시로 처리합니다. 실제로는 더 견고한 매핑 로직이 필요할 수 있습니다.
                String property = order.getProperty().replaceAll("([a-z])([A-Z]+)", "$1_$2").toLowerCase();
                sql.append(property).append(" ").append(order.getDirection().name()).append(", ");
            });
            sql.delete(sql.length() - 2, sql.length()); // 마지막 ", " 제거
        }

        Query query = entityManager.createNativeQuery(sql.toString(), SearchIndex.class);
        for (int i = 0; i < parameters.size(); i++) {
            query.setParameter(i + 1, parameters.get(i));
        }
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        return query.getResultList(); // 결과는 SearchIndex 엔티티 리스트로 자동 매핑됩니다.
    }

    private List<UUID> getFriendIds(String userId) {
        List<UUID> followingIds = followRepository.findFollowingIdsByFollowerId(userId);
        List<UUID> blockedIds = blockRepository.findBlockedIdsByBlockerId(userId);
        followingIds.removeAll(blockedIds);
        return followingIds;
    }
}
