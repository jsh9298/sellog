package com.teamproject.sellog.domain.post.service.event;

import java.util.UUID;

import org.springframework.context.ApplicationEvent;

public class PostDeletedEvent extends ApplicationEvent {
    private final UUID postId;

    public PostDeletedEvent(Object source, UUID postId) {
        super(source);
        this.postId = postId;
    }

    public UUID getPostId() {
        return postId;
    }
}