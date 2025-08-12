package com.teamproject.sellog.domain.post.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.teamproject.sellog.common.responseUtils.BusinessException;
import com.teamproject.sellog.common.responseUtils.ErrorCode;
import com.teamproject.sellog.domain.post.mapper.ReviewMapper;
import com.teamproject.sellog.domain.post.model.dto.request.ReviewRequest;
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
}