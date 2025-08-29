package com.teamproject.sellog.domain.auth.service.event;

import java.util.UUID;

import org.springframework.context.ApplicationEvent;

public class UserCreatedEvent extends ApplicationEvent {
    private final UUID userId;

    public UserCreatedEvent(Object source, UUID userId) {
        super(source);
        this.userId = userId;
    }

    public UUID getUserId() {
        return userId;
    }
}