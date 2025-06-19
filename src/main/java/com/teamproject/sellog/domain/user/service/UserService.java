package com.teamproject.sellog.domain.user.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.teamproject.sellog.auth.repository.AuthRepository;
import com.teamproject.sellog.domain.user.model.DTO.request.InfoInput;
import com.teamproject.sellog.domain.user.model.DTO.response.Response;
import com.teamproject.sellog.domain.user.model.user.User;
import com.teamproject.sellog.domain.user.model.user.UserPrivate;
import com.teamproject.sellog.domain.user.model.user.UserProfile;
import com.teamproject.sellog.domain.user.repository.UserPrivateRepository;
import com.teamproject.sellog.domain.user.repository.UserProfileRepository;

@Service
public class UserService {

        private final AuthRepository authRepository;
        private final UserProfileRepository userProfileRepository;
        private final UserPrivateRepository userPrivateRepository;

        public UserService(AuthRepository authRepository, UserProfileRepository userProfileRepository,
                        UserPrivateRepository userPrivateRepository) {
                this.authRepository = authRepository;
                this.userPrivateRepository = userPrivateRepository;
                this.userProfileRepository = userProfileRepository;
        }

        public Response getUserInfo(String userId) {
                UUID id = userIdToId(userId);
                String email = authRepository.findEmailByUserId(userId)
                                .orElseThrow(() -> new IllegalArgumentException("User not found"));
                UserPrivate userPriv = userPrivateRepository.findById(id)
                                .orElseThrow(() -> new IllegalArgumentException("User not found"));
                UserProfile userProf = userProfileRepository.findById(id)
                                .orElseThrow(() -> new IllegalArgumentException("User not found"));
                Response res = Response.builder()
                                .nickname(userProf.getNickname())
                                .profileMessage(userProf.getProfileMessage())
                                .profileThumbURL(userProf.getProfileThumbURL())
                                .profileURL(userProf.getProfileURL())
                                .userName(userPriv.getUserName())
                                .userAddress(userPriv.getUserAddress())
                                .phoneNumber(userPriv.getPhoneNumber())
                                .email(email)
                                .build();
                return res;
        }

        public Response setUserInfo(InfoInput input, String userId) {
                User user = authRepository.findByUserId(userId)
                                .orElseThrow(() -> new IllegalArgumentException("User not found"));
                UserPrivate userPriv = userPrivateRepository.findById(user.getId())
                                .orElseThrow(() -> new IllegalArgumentException("User not found"));
                UserProfile userProf = userProfileRepository.findById(user.getId())
                                .orElseThrow(() -> new IllegalArgumentException("User not found"));
                userPriv.setPhoneNumber(input.getPhoneNumber());
                userPriv.setUserAddress(input.getUserAddress());
                userPriv.setUserName(input.getUserName());

                userProf.setNickname(input.getNickname());
                userProf.setProfileMessage(input.getProfileMessage());
                userProf.setProfileThumbURL(input.getProfileThumbURL());
                userProf.setProfileURL(input.getProfileURL());

                user.setUserPrivate(userPriv);
                user.setUserProfile(userProf);
                user.setEmail(input.getEmail());
                authRepository.save(user);

                Response res = Response.builder()
                                .nickname(userProf.getNickname())
                                .profileMessage(userProf.getProfileMessage())
                                .profileThumbURL(userProf.getProfileThumbURL())
                                .profileURL(userProf.getProfileURL())
                                .userName(userPriv.getUserName())
                                .userAddress(userPriv.getUserAddress())
                                .phoneNumber(userPriv.getPhoneNumber())
                                .email(user.getEmail())
                                .build();

                return res;
        }

        public Response getUserPrivate(String userId) {
                UserPrivate userPriv = userPrivateRepository.findById(userIdToId(userId))
                                .orElseThrow(() -> new IllegalArgumentException("User not found"));
                Response res = Response.builder()
                                .userName(userPriv.getUserName())
                                .userAddress(userPriv.getUserAddress())
                                .phoneNumber(userPriv.getPhoneNumber())
                                .build();
                return res;
        }

        public Response setUserPrivate(InfoInput input, String userId) {
                User user = authRepository.findByUserId(userId)
                                .orElseThrow(() -> new IllegalArgumentException("User not found"));
                UserPrivate userPriv = userPrivateRepository.findById(user.getId())
                                .orElseThrow(() -> new IllegalArgumentException("User not found"));
                userPriv.setPhoneNumber(input.getPhoneNumber());
                userPriv.setUserAddress(input.getUserAddress());
                userPriv.setUserName(input.getUserName());

                user.setUserPrivate(userPriv);

                authRepository.save(user);

                Response res = Response.builder()
                                .userName(userPriv.getUserName())
                                .userAddress(userPriv.getUserAddress())
                                .phoneNumber(userPriv.getPhoneNumber())
                                .build();
                return res;
        }

        public Response getUserProfile(String userId) {
                UserProfile userProf = userProfileRepository.findById(userIdToId(userId))
                                .orElseThrow(() -> new IllegalArgumentException("User not found"));
                Response res = Response.builder()
                                .nickname(userProf.getNickname())
                                .profileMessage(userProf.getProfileMessage())
                                .profileThumbURL(userProf.getProfileThumbURL())
                                .profileURL(userProf.getProfileURL())
                                .build();
                return res;
        }

        public Response setUserProfile(InfoInput input, String userId) {

                User user = authRepository.findByUserId(userId)
                                .orElseThrow(() -> new IllegalArgumentException("User not found"));
                UserProfile userProf = userProfileRepository.findById(user.getId())
                                .orElseThrow(() -> new IllegalArgumentException("User not found"));

                Response res = Response.builder()
                                .nickname(userProf.getNickname())
                                .profileMessage(userProf.getProfileMessage())
                                .profileThumbURL(userProf.getProfileThumbURL())
                                .profileURL(userProf.getProfileURL())
                                .build();
                return res;
        }

        private UUID userIdToId(String userId) {
                return authRepository.findIdByUserId(userId)
                                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        }
}
