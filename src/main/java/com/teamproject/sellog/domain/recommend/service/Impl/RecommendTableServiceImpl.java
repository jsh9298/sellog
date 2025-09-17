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
