package com.teamproject.sellog.domain.post.service.event;

import lombok.Getter;

import java.util.UUID;

import org.springframework.context.ApplicationEvent;

@Getter
public class ReviewUpdatedEvent extends ApplicationEvent {
    private final UUID reviewId;

    public ReviewUpdatedEvent(Object source, UUID reviewId) {
        super(source);
        this.reviewId = reviewId;
    }
}