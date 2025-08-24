package com.teamproject.sellog.domain.post.service.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
public class PostDislikedEvent extends ApplicationEvent {
    private final UUID postId;
    private final String userId;
    private final boolean disliked; // true: 싫어요, false: 싫어요 취소

    public PostDislikedEvent(Object source, UUID postId, String userId, boolean disliked) {
        super(source);
        this.postId = postId;
        this.userId = userId;
        this.disliked = disliked;
    }
}