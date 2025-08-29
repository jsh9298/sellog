package com.teamproject.sellog.domain.auth.service.event;

import java.util.UUID;

import org.springframework.context.ApplicationEvent;

public class UserDeletedEvent extends ApplicationEvent {
    private final UUID userId;

    public UserDeletedEvent(Object source, UUID userId) {
        super(source);
        this.userId = userId;
    }

    public UUID getUserId() {
        return userId;
    }
}