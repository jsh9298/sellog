package com.teamproject.sellog.domain.post.service.event;

import com.teamproject.sellog.domain.post.model.entity.Review;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ReviewUpdatedEvent extends ApplicationEvent {
    private final Review review;

    public ReviewUpdatedEvent(Object source, Review review) {
        super(source);
        this.review = review;
    }
}