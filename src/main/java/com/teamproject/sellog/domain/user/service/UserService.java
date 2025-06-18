package com.teamproject.sellog.domain.user.service;

import org.springframework.stereotype.Service;

import com.teamproject.sellog.auth.repository.AuthRepository;
import com.teamproject.sellog.domain.user.model.DTO.request.InfoInput;
import com.teamproject.sellog.domain.user.model.DTO.response.Response;
import com.teamproject.sellog.domain.user.repository.UserPrivateRepository;
import com.teamproject.sellog.domain.user.repository.UserProfileRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AuthRepository authRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserPrivateRepository userPrivateRepository;

    public InfoInput getUserInfo(String userId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getUserInfo'");
    }

    public InfoInput setUserInfo(Response input, String userId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setUserInfo'");
    }

    public InfoInput getUserPrivate(String userId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getUserPrivate'");
    }

    public InfoInput setUserPrivate(Response input, String userId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setUserPrivate'");
    }

    public InfoInput getUserProfile(String userId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getUserProfile'");
    }

    public InfoInput setUserProfile(Response input, String userId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setUserProfile'");
    }
}
