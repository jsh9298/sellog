package com.teamproject.sellog.domain.user.service;

import org.springframework.stereotype.Service;

import com.teamproject.sellog.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FollowBlockService {
    private final UserRepository userRepository;

    public void addFollower() {
    }
}
