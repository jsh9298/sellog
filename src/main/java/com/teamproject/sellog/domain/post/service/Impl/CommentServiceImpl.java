package com.teamproject.sellog.domain.post.service.Impl;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.UUID;
import java.util.stream.Collectors;

import com.teamproject.sellog.common.responseUtils.CursorPageResponse;
import com.teamproject.sellog.domain.post.model.dto.response.CommentResponse;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.PageRequest;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public void comment(CommentRequest dto, UUID postId, UUID parentId, String userId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Comment newComment = new Comment();
        newComment.setContent(dto.getContent());
        newComment.setAuthor(user);
        newComment.setPost(post);

        if (parentId != null) { // 대댓글인 경우
            Comment parent = commentRepository.findById(parentId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

            // 삭제된 댓글에는 답글을 달 수 없도록 처리 (isDeleted()는 Comment 엔티티에 구현 가정)
            if (parent.isDeleted()) {
                throw new BusinessException(ErrorCode.COMMENT_IS_DELETED);
            }

            newComment.setParentId(parent.getId());
            newComment.setGroupId(parent.getGroupId()); // 답글은 부모의 그룹 ID를 상속
            newComment.setDepth(parent.getDepth().add(BigInteger.ONE));
        } else { // 최상위 댓글인 경우
            newComment.setParentId(null);
            newComment.setDepth(BigInteger.valueOf(0));
        }

        Comment savedComment = commentRepository.save(newComment);
        if (savedComment.getGroupId() == null) {
            savedComment.setGroupId(savedComment.getId()); // 최상위 댓글은 자신의 ID를 그룹 ID로 가짐
        }
    }

    @Transactional
    public void editComment(CommentRequest dto, UUID commentId, String userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

        if (!comment.getAuthor().getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.COMMENT_OWNER_MISMATCH);
        }
        comment.setContent(dto.getContent());
    }

    @Transactional
    public void deleteComment(UUID commentId, String userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));
        if (!comment.getAuthor().getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.COMMENT_OWNER_MISMATCH);
        }
        comment.delete(); // 물리적 삭제(delete) 대신 논리적 삭제(delete) 처리
    }

    @Transactional(readOnly = true)
    public CursorPageResponse<CommentResponse> listComment(UUID postId, UUID lastGroupId, Timestamp lastCreateAt,
            UUID lastId,
            int limit) {
        if (!postRepository.existsById(postId)) {
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }

        // 정렬 기준: 그룹 ID 오름차순, 생성 시간 오름차순, ID 오름차순 (계층 구조를 위한 정렬)
        Sort sort = Sort.by(Sort.Direction.ASC, "groupId", "createAt", "id");
        // 다음 페이지 확인을 위해 요청된 사이즈보다 하나 더 많이 조회
        Pageable pageable = PageRequest.of(0, limit + 1, sort);

        // 동적 쿼리를 위한 Specification 생성
        Specification<Comment> spec = (root, query, cb) -> {
            Predicate postPredicate = cb.equal(root.get("post").get("id"), postId);

            // 커서 값이 있으면, 커서 기반으로 다음 페이지 조회 조건 추가
            if (lastGroupId != null && lastCreateAt != null && lastId != null) {
                // (groupId > lastGroupId) OR
                // (groupId = lastGroupId AND createAt > lastCreateAt) OR
                // (groupId = lastGroupId AND createAt = lastCreateAt AND id > lastId)
                Predicate groupPredicate = cb.greaterThan(root.get("groupId"), lastGroupId);

                Predicate timePredicate = cb.and(
                        cb.equal(root.get("groupId"), lastGroupId),
                        cb.greaterThan(root.get("createAt"), lastCreateAt));

                Predicate idPredicate = cb.and(
                        cb.equal(root.get("groupId"), lastGroupId),
                        cb.equal(root.get("createAt"), lastCreateAt),
                        cb.greaterThan(root.get("id"), lastId));

                return cb.and(postPredicate, cb.or(groupPredicate, timePredicate, idPredicate));
            }
            return postPredicate;
        };

        // JpaSpecificationExecutor를 사용하여 데이터 조회
        List<Comment> comments = commentRepository.findAll(spec, pageable).getContent();

        boolean hasNext = comments.size() > limit;
        List<Comment> pageContent = hasNext ? comments.subList(0, limit) : comments;

        List<CommentResponse> responseContent = pageContent.stream()
                .map(CommentResponse::fromEntity)
                .collect(Collectors.toList());

        UUID nextCursorGroupId = hasNext ? pageContent.get(limit - 1).getGroupId() : null;
        Timestamp nextCursorCreateAt = hasNext ? pageContent.get(limit - 1).getCreateAt() : null;
        UUID nextCursorId = hasNext ? pageContent.get(limit - 1).getId() : null;

        return new CursorPageResponse<>(responseContent, hasNext, nextCursorGroupId, nextCursorCreateAt, nextCursorId);
    }
}
