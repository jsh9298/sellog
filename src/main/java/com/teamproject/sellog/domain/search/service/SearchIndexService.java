package com.teamproject.sellog.domain.search.service;

import com.teamproject.sellog.domain.post.model.entity.Post; // Post import
import com.teamproject.sellog.domain.auth.service.event.UserCreatedEvent;
import com.teamproject.sellog.domain.auth.service.event.UserDeletedEvent;
import com.teamproject.sellog.domain.post.model.entity.HashBoard; // HashBoard import
import com.teamproject.sellog.domain.post.model.entity.HashTag; // HashTag import
import com.teamproject.sellog.domain.search.model.entity.SearchIndex; // SearchIndex import
import com.teamproject.sellog.domain.search.repository.SearchIndexRepository; // SearchIndexRepository import
import com.teamproject.sellog.domain.user.model.entity.user.User; // User import
import com.teamproject.sellog.domain.post.repository.PostRepository; // PostRepository import
import com.teamproject.sellog.domain.post.service.event.PostCreatedEvent;
import com.teamproject.sellog.domain.post.service.event.PostDeletedEvent;
import com.teamproject.sellog.domain.post.service.event.PostUpdatedEvent;
import com.teamproject.sellog.domain.user.repository.UserRepository; // UserRepository import
import com.teamproject.sellog.domain.user.service.event.UserUpdatedEvent;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchIndexService {

    private final SearchIndexRepository searchIndexRepository;
    private final PostRepository postRepository; // 태그 정보 등 가져오기 위함
    private final UserRepository userRepository; // 사용자 정보 가져오기 위함

    // --- Post 엔티티 변경 이벤트 처리 ---
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePostCreated(PostCreatedEvent event) {
        updateSearchIndexForPost(event.getPost());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePostUpdated(PostUpdatedEvent event) {
        updateSearchIndexForPost(event.getPost());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePostDeleted(PostDeletedEvent event) {
        searchIndexRepository.deleteBySourceIdAndSourceType(event.getPostId(), "POST");
    }

    // --- User 엔티티 변경 이벤트 처리 (예시) ---
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserUpdated(UserUpdatedEvent event) {
        updateSearchIndexForUser(event.getUser());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserCreated(UserCreatedEvent event) {
        updateSearchIndexForUser(event.getUser());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserDeleted(UserDeletedEvent event) {
        searchIndexRepository.deleteBySourceIdAndSourceType(event.getUser().getId(), "USER");
    }

    // --- SearchIndex 업데이트 로직 ---
    @Transactional
    public void updateSearchIndexForPost(Post post) {
        // 기존 인덱스 조회 또는 새로 생성
        SearchIndex searchIndex = searchIndexRepository.findBySourceIdAndSourceType(post.getId(), "POST")
                .orElseGet(SearchIndex::new);

        // SearchIndex 필드 설정
        searchIndex.setSourceId(post.getId());
        searchIndex.setSourceType("POST");
        searchIndex.setMainTitle(post.getTitle());
        searchIndex.setSubContent(post.getContent()); // 본문 전체를 subContent로
        searchIndex.setThumbnailUrl(post.getThumbnail());
        searchIndex.setCreatedAt(post.getCreateAt());
        searchIndex.setLikeCount(post.getLikeCnt());
        searchIndex.setAuthorId(post.getAuthor() != null ? post.getAuthor().getUserId() : null); // 작성자 ID
        searchIndex.setAuthorNickname(post.getAuthor().getUserProfile().getNickname());
        searchIndex.setLocationPoint(post.getLocationPoint()); // JTS Point
        searchIndex.setPrice(post.getPrice());

        // Full-Text Content 조합 (Post Title, Content, HashTag Names)
        String fullText = post.getTitle() + " " + post.getContent();
        // Post에 연결된 HashTag 정보를 가져와서 fullText에 추가
        // Post.hashBoard (Set<HashBoard>)는 Lazy Loading이므로 트랜잭션 범위 내에서 접근
        // 또는 PostRepository에서 Post를 Eager Fetch로 다시 조회
        if (post.getHashBoard() != null) { // Lazy Loading 이슈로 NPE 방지
            String tagNames = post.getHashBoard().stream()
                    .map(HashBoard::getTag)
                    .filter(Objects::nonNull) // 태그가 null이 아닐 때만
                    .map(HashTag::getTagName)
                    .collect(Collectors.joining(" "));
            fullText += " " + tagNames;
        }
        searchIndex.setFullTextContent(fullText.trim());

        searchIndexRepository.save(searchIndex);

    }

    @Transactional
    public void updateSearchIndexForUser(User user) {
        SearchIndex searchIndex = searchIndexRepository.findBySourceIdAndSourceType(user.getId(), "USER")
                .orElseGet(SearchIndex::new);

        searchIndex.setSourceId(user.getId());
        searchIndex.setSourceType("USER");
        searchIndex.setMainTitle(user.getUserProfile().getNickname()); // 사용자 이름
        searchIndex.setSubContent(user.getEmail()); // 이메일 등 추가 정보
        searchIndex.setCreatedAt(null); // User 생성 시간 필드 있다면 추가
        searchIndex.setAuthorId(user.getUserId()); // 사용자 자신 ID를 authorId에 저장
        searchIndex.setAuthorNickname(user.getUserProfile().getNickname());
        searchIndex.setLocationPoint(user.getUserPrivate().getLocationPoint()); // 사용자 JTS Point

        // Full-Text Content 조합 (사용자 이름, 이메일 등)
        String fullText = user.getUserProfile().getNickname() + " " + user.getEmail();
        searchIndex.setFullTextContent(fullText.trim());

        searchIndexRepository.save(searchIndex);

    }
}