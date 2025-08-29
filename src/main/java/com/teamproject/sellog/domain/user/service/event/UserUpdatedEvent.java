package com.teamproject.sellog.domain.user.service.event;

import java.util.UUID;

import org.springframework.context.ApplicationEvent;

public class UserUpdatedEvent extends ApplicationEvent {
    private final UUID userId;

    public UserUpdatedEvent(Object source, UUID userId) {
        super(source);
        this.userId = userId;
    }

    public UUID getUserId() {
        return userId;
    }
}