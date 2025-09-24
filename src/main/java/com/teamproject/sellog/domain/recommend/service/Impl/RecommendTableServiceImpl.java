package com.teamproject.sellog.domain.recommend.service.Impl;

import com.teamproject.sellog.domain.post.model.entity.Post;
import com.teamproject.sellog.domain.post.repository.PostRepository;
import com.teamproject.sellog.domain.post.repository.ReviewRepository;
import com.teamproject.sellog.domain.post.service.event.PostCreatedEvent;
import com.teamproject.sellog.domain.post.service.event.PostDislikedEvent;
import com.teamproject.sellog.domain.post.service.event.PostLikedEvent;
import com.teamproject.sellog.domain.post.service.event.PostUpdatedEvent;
import com.teamproject.sellog.domain.post.service.event.ReviewCreatedEvent;
import com.teamproject.sellog.domain.post.service.event.ReviewDeletedEvent;
import com.teamproject.sellog.domain.post.service.event.PostViewedEvent; // 조회 이벤트 추가
import com.teamproject.sellog.domain.recommend.repository.ItemRepository;
import com.teamproject.sellog.domain.recommend.repository.UserInteractionRepository;
import com.teamproject.sellog.domain.recommend.service.RecommendTableService;
import com.teamproject.sellog.domain.search.model.entity.SearchIndex;
import com.teamproject.sellog.domain.search.service.event.SearchIndexEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendTableServiceImpl implements RecommendTableService {

    private final UserInteractionRepository userInteractionRepository;
    private final ItemRepository itemRepository;
    private final PostRepository postRepository;
    private final ReviewRepository reviewRepository;

    /*
     * 수집이 필요한 이벤트 종류 정리
     * 1.게시물 작성/수정/삭제
     * 2.조회 내역 및 체류 시간
     * 3.좋아요,싫어요 내역
     * 4.리뷰작성 내역 및 후기 점수
     * 5.검색어 내역 및 사용빈도?
     * 6.맞팔 목록
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handlePostCreated(PostCreatedEvent event) {
        // postRepository.findById(event.getPostId()).ifPresent(this::updateItemForPost);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handlePostUpdated(PostUpdatedEvent event) {
        // postRepository.findById(event.getPostId()).ifPresent(this::updateItemForPost);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleUpdateSearchIndex(SearchIndexEvent event) {
    }

    @Transactional
    public void updateItemForPost(Post post) {
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handlePostLiked(PostLikedEvent event) {
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handlePostViewed(PostViewedEvent event) {
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleReviewCreated(ReviewCreatedEvent event) {

    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleReviewDeleted(ReviewDeletedEvent event) {

    }

    @Transactional
    public void updateItemForSearchIndex(SearchIndex searchIndex) {

    }

    @Transactional
    public void updateUserInteraction() {
    }

    @Override
    public void handlePostDisliked(PostDislikedEvent event) {
        throw new UnsupportedOperationException("Unimplemented method 'handlePostDisliked'");
    }
}
