package com.teamproject.sellog.domain.post.service.event;

import org.springframework.context.ApplicationEvent;

import com.teamproject.sellog.domain.post.model.entity.Post;

public class PostUpdatedEvent extends ApplicationEvent {
    private final Post post;

    public PostUpdatedEvent(Object source, Post post) {
        super(source);
        this.post = post;
    }

    public Post getPost() {
        return post;
    }
}
