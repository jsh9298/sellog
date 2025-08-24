package com.teamproject.sellog.domain.post.service.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
public class PostLikedEvent extends ApplicationEvent {
    private final UUID postId;
    private final String userId;
    private final boolean liked; // true: 좋아요, false: 좋아요 취소

    public PostLikedEvent(Object source, UUID postId, String userId, boolean liked) {
        super(source);
        this.postId = postId;
        this.userId = userId;
        this.liked = liked;
    }
}