package com.teamproject.sellog.domain.post.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.teamproject.sellog.common.responseUtils.BusinessException;
import com.teamproject.sellog.common.responseUtils.ErrorCode;
import com.teamproject.sellog.domain.post.model.dto.request.CommentRequest;
import com.teamproject.sellog.domain.post.model.entity.Comment;
import com.teamproject.sellog.domain.post.model.entity.Post;
import com.teamproject.sellog.domain.post.repository.CommentRepository;
import com.teamproject.sellog.domain.post.repository.PostRepository;
import com.teamproject.sellog.domain.user.model.entity.user.User;
import com.teamproject.sellog.domain.user.repository.UserRepository;

@Service
public class CommentService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    public CommentService(final PostRepository postRepository, final UserRepository userRepository,
            final CommentRepository commentRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
    }

    public void review(CommentRequest dto, UUID postId, UUID commentId) {
        Comment newComment = new Comment();
        Post post = postRepository.findById(postId).orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
        User user = userRepository.findByUserId(dto.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        newComment.setContent(dto.getContent());
        newComment.setAuthor(user);
        newComment.setPost(post);
        commentRepository.save(newComment);
    }
}
