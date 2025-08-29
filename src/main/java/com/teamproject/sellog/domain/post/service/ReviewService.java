package com.teamproject.sellog.domain.post.service;

import com.teamproject.sellog.common.responseUtils.BusinessException;
import com.teamproject.sellog.common.responseUtils.CursorPageResponse;
import com.teamproject.sellog.common.responseUtils.ErrorCode;
import com.teamproject.sellog.domain.post.model.dto.request.ReviewRequest;
import com.teamproject.sellog.domain.post.model.dto.response.ReviewResponse;
import com.teamproject.sellog.domain.post.model.entity.Post;
import com.teamproject.sellog.domain.post.model.entity.PostType;
import com.teamproject.sellog.domain.post.model.entity.Review;
import com.teamproject.sellog.domain.post.repository.PostRepository;
import com.teamproject.sellog.domain.post.repository.ReviewRepository;
import com.teamproject.sellog.domain.post.service.event.ReviewCreatedEvent;
import com.teamproject.sellog.domain.post.service.event.ReviewDeletedEvent;
import com.teamproject.sellog.domain.post.service.event.ReviewUpdatedEvent;
import com.teamproject.sellog.domain.user.model.entity.user.User;
import com.teamproject.sellog.domain.user.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public CursorPageResponse<ReviewResponse> listReview(UUID postId, Timestamp lastCreateAt, UUID lastId, int limit) {
        if (!postRepository.existsById(postId)) {
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }

        Sort sort = Sort.by(Sort.Direction.DESC, "createAt", "id");
        Pageable pageable = PageRequest.of(0, limit + 1, sort);

        Specification<Review> spec = (root, query, cb) -> {
            Predicate predicate = cb.equal(root.get("post").get("id"), postId);
            if (lastCreateAt != null && lastId != null) {
                Predicate timePredicate = cb.lessThan(root.get("createAt"), lastCreateAt);
                Predicate tieBreaker = cb.and(cb.equal(root.get("createAt"), lastCreateAt),
                        cb.lessThan(root.get("id"), lastId));
                predicate = cb.and(predicate, cb.or(timePredicate, tieBreaker));
            }
            return predicate;
        };

        List<Review> reviews = reviewRepository.findAll(spec, pageable).getContent();

        boolean hasNext = reviews.size() > limit;
        List<Review> content = hasNext ? reviews.subList(0, limit) : reviews;

        List<ReviewResponse> responseContent = content.stream()
                .map(ReviewResponse::fromEntity)
                .collect(Collectors.toList());

        Timestamp nextCursorCreateAt = hasNext ? content.get(limit - 1).getCreateAt() : null;
        UUID nextCursorId = hasNext ? content.get(limit - 1).getReviewId() : null;

        return new CursorPageResponse<>(responseContent, hasNext, null, nextCursorCreateAt, nextCursorId);
    }

    @Transactional
    public void createReview(UUID postId, String userId, ReviewRequest dto) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // --- 비즈니스 규칙 검증 ---
        if (post.getPostType() != PostType.PRODUCT) {
            throw new BusinessException(ErrorCode.CANNOT_REVIEW_NON_PRODUCT);
        }
        if (post.getAuthor().getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.CANNOT_REVIEW_OWN_POST);
        }
        if (reviewRepository.existsByPostAndAuthor(post, user)) {
            throw new BusinessException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        Review review = new Review(); // Review 엔티티에 Builder가 없다면 new로 생성
        review.setContent(dto.getContent());
        review.setScore(dto.getRating());
        review.setPost(post);
        review.setAuthor(user);

        Review savedReview = reviewRepository.save(review);

        eventPublisher.publishEvent(new ReviewCreatedEvent(this, savedReview.getReviewId()));
    }

    @Transactional
    public void editReview(UUID reviewId, String userId, ReviewRequest dto) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));

        if (!review.getAuthor().getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.REVIEW_OWNER_MISMATCH);
        }

        review.setContent(dto.getContent());
        review.setScore(dto.getRating());

        eventPublisher.publishEvent(new ReviewUpdatedEvent(this, review.getReviewId()));
    }

    @Transactional
    public void deleteReview(UUID reviewId, String userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));

        if (!review.getAuthor().getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.REVIEW_OWNER_MISMATCH);
        }
        eventPublisher.publishEvent(new ReviewDeletedEvent(this, review.getReviewId()));
        reviewRepository.delete(review); // 리뷰는 자식 엔티티가 없으므로 물리적 삭제도 가능
    }
}