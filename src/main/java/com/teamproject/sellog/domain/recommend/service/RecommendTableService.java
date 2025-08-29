package com.teamproject.sellog.domain.recommend.service;

import com.teamproject.sellog.domain.post.model.entity.Post;
import com.teamproject.sellog.domain.post.model.entity.PostType;
import com.teamproject.sellog.domain.post.model.entity.Review;
import com.teamproject.sellog.domain.post.service.event.PostDislikedEvent;
import com.teamproject.sellog.domain.post.service.event.PostCreatedEvent;
import com.teamproject.sellog.domain.post.service.event.PostLikedEvent;
import com.teamproject.sellog.domain.post.service.event.PostUpdatedEvent;
import com.teamproject.sellog.domain.post.service.event.ReviewCreatedEvent;
import com.teamproject.sellog.domain.post.service.event.ReviewDeletedEvent;
import com.teamproject.sellog.domain.recommend.model.Item;
import com.teamproject.sellog.domain.recommend.repository.ItemRepository;
import com.teamproject.sellog.domain.recommend.model.UserInteraction;
import com.teamproject.sellog.domain.recommend.repository.UserInteractionRepository;
import com.teamproject.sellog.domain.search.model.entity.SearchIndex;
import com.teamproject.sellog.domain.search.service.event.SearchIndexEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendTableService {

    // post,hashtag,searchParam,trade_process,user,comment,review
    private final UserInteractionRepository userInteractionRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePostCreated(PostCreatedEvent event) {
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePostUpdated(PostUpdatedEvent event) {

    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUpdateSearchIndex(SearchIndexEvent event) {

    }

    @Transactional
    public void updateItemForPost(Post post) {
        // 게시물이 추천 대상(예: 판매 상품)인 경우에만 아이템 테이블에 추가/업데이트
        if (post.getPostType() == PostType.PRODUCT) {
            // Item item = itemRepository.findById(post.getId()).orElse(new
            // Item(post.getId()));
            // item.setTitle(post.getTitle());
            // item.setContent(post.getContent());
            // item.setPrice(post.getPrice());
            // ... 기타 추천에 필요한 메타데이터 매핑
            // itemRepository.save(item);

        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePostLiked(PostLikedEvent event) {
        // log.info("PostLikedEvent received for post ID: {}, user ID: {}, liked: {}",
        // event.getPostId(), event.getUserId(), event.isLiked());

        // if (event.isLiked()) {
        // UserInteraction interaction = UserInteraction.builder()
        // .userId(event.getUserId())
        // .itemId(event.getPostId())
        // .interactionType("like")
        // .build();
        // userInteractionRepository.save(interaction);
        // } else {
        // userInteractionRepository.deleteByUserIdAndItemIdAndInteractionType(event.getUserId(),
        // event.getPostId(),
        // "like");
        // }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePostDisliked(PostDislikedEvent event) {
        // log.info("PostDislikedEvent received for post ID: {}, user ID: {}, disliked:
        // {}",
        // event.getPostId(), event.getUserId(), event.isDisliked());

        // if (event.isDisliked()) {
        // UserInteraction interaction = UserInteraction.builder()
        // .userId(event.getUserId())
        // .itemId(event.getPostId())
        // .interactionType("dislike")
        // .build();
        // userInteractionRepository.save(interaction);
        // } else {
        // userInteractionRepository.deleteByUserIdAndItemIdAndInteractionType(event.getUserId(),
        // event.getPostId(),
        // "dislike");
        // }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleReviewCreated(ReviewCreatedEvent event) {
        // Review review = event.getReviewId();
        // log.info("ReviewCreatedEvent received for review ID: {}, user ID: {}",
        // review.getReviewId(),
        // review.getAuthor().getUserId());

        // UserInteraction interaction = UserInteraction.builder()
        // .userId(review.getAuthor().getUserId())
        // .itemId(review.getPost().getId())
        // .interactionType("review")
        // .build();
        // .value(Double.valueOf(review.getRating())); // 평점을 상호작용 값으로 저장
        // userInteractionRepository.save(interaction);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleReviewDeleted(ReviewDeletedEvent event) {
        // Review review = event.getReview();
        // log.info("ReviewDeletedEvent received for review ID: {}, user ID: {}",
        // review.getReviewId(),
        // review.getAuthor().getUserId());

        // // 리뷰 삭제 시 상호작용 데이터도 삭제
        // userInteractionRepository.deleteByUserIdAndItemIdAndInteractionType(
        // review.getAuthor().getUserId(),
        // review.getPost().getId(),
        // "review");
    }

    @Transactional
    public void updateItemForSearchIndex(SearchIndex searchIndex) {
        // SearchIndex는 Post 외에 User도 포함할 수 있으므로,
        // Post에 대한 정보는 handlePostCreated/Updated에서 처리하는 것이 더 명확할 수 있습니다.
        // 이 핸들러는 다른 유형의 추천 아이템(예: 사용자 추천)에 사용될 수 있습니다.
        log.warn("updateItemForSearchIndex is not fully implemented yet.");
    }

    @Transactional
    public void updateUserInteraction() {
        // 이 메서드는 사용자의 행동(좋아요, 조회, 댓글 등)을
        // 추천 시스템이 사용할 수 있는 형태로 가공하여 저장하는 로직을 포함해야 합니다.
        // 예: 특정 시간마다 배치(batch) 작업으로 실행
        log.info("Updating user interactions for recommendation model.");
    }
}
