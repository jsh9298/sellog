package com.teamproject.sellog.domain.post.service;

import java.math.BigInteger;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public void comment(CommentRequest dto, UUID postId, UUID commentId) {
        Comment newComment = new Comment();
        Optional<Comment> parentComment = commentRepository.findById(commentId);
        Post post = postRepository.findById(postId).orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
        User user = userRepository.findByUserId(dto.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        newComment.setContent(dto.getContent());
        newComment.setAuthor(user);
        newComment.setPost(post);
        if (parentComment.isPresent()) {
            newComment.setParentId(parentComment.get().getId());
            newComment.setGroupId(parentComment.get().getGroupId());
            newComment.setDepth(parentComment.get().getDepth().add(BigInteger.valueOf('1')));
            newComment.setSortOrder(parentComment.get().getSortOrder().add(BigInteger.valueOf('1')));
        } else {
            newComment.setParentId(null);
            newComment.setDepth(BigInteger.valueOf(0));
            newComment.setSortOrder(BigInteger.valueOf(0));
        }
        commentRepository.save(newComment);
    }

    @Transactional
    public void editComment(CommentRequest dto, UUID commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow();
        comment.setContent(dto.getContent());
    }

    public void listCommnet() {

    }
}
