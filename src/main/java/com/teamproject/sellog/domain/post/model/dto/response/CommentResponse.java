package com.teamproject.sellog.domain.post.model.dto.response;

import com.teamproject.sellog.domain.post.model.entity.Comment;
import lombok.Builder;
import lombok.Getter;

import java.sql.Timestamp;
import java.util.UUID;

@Getter
@Builder
public class CommentResponse {
    private final UUID commentId;
    private final String content;
    private final String authorNickname;
    private final String authorProfileImage;
    private final UUID authorId;
    private final Timestamp createdAt;
    private final UUID parentId;
    private final UUID groupId;
    private final int depth;

    public static CommentResponse fromEntity(Comment comment) {
        // isDeleted() 메서드는 Comment 엔티티에 구현되어 있다고 가정합니다.
        if (comment.isDeleted()) {
            return CommentResponse.builder()
                    .commentId(comment.getId())
                    .content("삭제된 댓글입니다.")
                    .authorNickname("알 수 없음")
                    .authorProfileImage(null) // 기본 이미지 URL 또는 null
                    .authorId(null)
                    .createdAt(comment.getCreateAt())
                    .parentId(comment.getParentId())
                    .groupId(comment.getGroupId())
                    .depth(comment.getDepth().intValue())
                    .build();
        } else {
            return CommentResponse.builder()
                    .commentId(comment.getId())
                    .content(comment.getContent())
                    .authorNickname(comment.getAuthor().getUserProfile().getNickname())
                    .authorProfileImage(comment.getAuthor().getUserProfile().getProfileThumbURL())
                    .authorId(comment.getAuthor().getId())
                    .createdAt(comment.getCreateAt())
                    .parentId(comment.getParentId())
                    .groupId(comment.getGroupId())
                    .depth(comment.getDepth().intValue())
                    .build();
        }
    }
}