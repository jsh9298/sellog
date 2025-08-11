package com.teamproject.sellog.domain.post.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class CommentRequest {
    private String userId; // 작성자
    private String content;
}
