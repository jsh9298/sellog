package com.teamproject.sellog.domain.post.service.event;

import java.util.UUID;

import org.springframework.context.ApplicationEvent;

public class PostUpdatedEvent extends ApplicationEvent {
    private final UUID postId;

    public PostUpdatedEvent(Object source, UUID postId) {
        super(source);
        this.postId = postId;
    }

    public UUID getPostId() {
        return postId;
    }
}
