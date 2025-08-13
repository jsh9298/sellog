package com.teamproject.sellog.domain.post.service;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.teamproject.sellog.common.responseUtils.BusinessException;
import com.teamproject.sellog.common.responseUtils.CursorPageResponse;
import com.teamproject.sellog.common.responseUtils.ErrorCode;
import com.teamproject.sellog.domain.post.mapper.ReviewMapper;
import com.teamproject.sellog.domain.post.model.dto.request.ReviewRequest;
import com.teamproject.sellog.domain.post.model.dto.response.ReviewListResponse;
import com.teamproject.sellog.domain.post.model.dto.response.ReviewResponseDto;
import com.teamproject.sellog.domain.post.model.entity.Review;
import com.teamproject.sellog.domain.post.model.entity.Post;
import com.teamproject.sellog.domain.user.model.entity.user.User;
import com.teamproject.sellog.domain.post.repository.PostRepository;
import com.teamproject.sellog.domain.post.repository.ReviewRepository;
import com.teamproject.sellog.domain.user.repository.UserRepository;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final ReviewMapper reviewMapper;

    public ReviewService(final ReviewRepository reviewRepository, final PostRepository postRepository,
            final UserRepository userRepository, final ReviewMapper reviewMapper) {
        this.reviewRepository = reviewRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.reviewMapper = reviewMapper;
    }

    @Transactional
    public void review(ReviewRequest dto, UUID postId, String userId) { // 리뷰 작성
        Review newReview = new Review();
        Post post = postRepository.findById(postId).orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (reviewRepository.existsByAuthor(user)) {
            throw new BusinessException(ErrorCode.REVIEW_DENY_MULTIPLE);
        }
        newReview.setContent(dto.getContents());
        newReview.setScore(dto.getScore());
        newReview.setAuthor(user);
        newReview.setPost(post);
        reviewRepository.save(newReview);
    }

    @Transactional
    public ReviewResponseDto edit(ReviewRequest dto, UUID postId, String userId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Review review = reviewRepository.findByPostAndAuthor(post, user)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));
        reviewMapper.updateReviewFromRequest(review, dto);
        return reviewMapper.EntityToResponse(review);
    }

    @Transactional
    public void deleteReview(UUID postId, String userId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Review review = reviewRepository.findByPostAndAuthor(post, user)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));
        reviewRepository.delete(review);
    }

    @Transactional(readOnly = true)
    public CursorPageResponse<ReviewListResponse> listReview(UUID postId, Timestamp lastCreateAt, UUID lastId,
            int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createAt", "id"));
        List<Review> reviews;
        if(lastCreateAt == null && lastId == null){
            reviews = reviewRepository.findById()
        }else{
              reviews = reviewRepository.findById()
        }
        List<ReviewListResponse> reviewDto = reviews.stream().map(
            //맵퍼객체
        ).collect(Collectors.toList());
        boolean hasNext = reviews.size() == limit;
        Timestamp nextCreateAt = null;
        UUID nextId = null;
        if (hasNext) {
        Review lastReview = reviews.get(reviews.size() - 1);
        nextCreateAt = lastReview.getCreateAt();
        nextId = lastReview.getReviewId();
        }
        return  new CursorPageResponse<>(reviewDto, hasNext, nextCreateAt, nextId);

    }
}