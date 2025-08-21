package com.teamproject.sellog.domain.user.service;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.teamproject.sellog.common.accountsUtils.CheckStatus;
import com.teamproject.sellog.common.responseUtils.BusinessException;
import com.teamproject.sellog.common.responseUtils.CursorPageResponse;
import com.teamproject.sellog.common.responseUtils.ErrorCode;
import com.teamproject.sellog.domain.post.model.entity.Post;
import com.teamproject.sellog.domain.post.model.entity.PostType;
import com.teamproject.sellog.domain.post.repository.PostRepository;
import com.teamproject.sellog.domain.user.mapper.UserInfoMapper;
import com.teamproject.sellog.domain.user.model.dto.UserContentCount;
import com.teamproject.sellog.domain.user.model.dto.request.UserProfileRequest;
import com.teamproject.sellog.domain.user.model.dto.response.SimplePostList;
import com.teamproject.sellog.domain.user.model.dto.response.UserPreviewResponse;
import com.teamproject.sellog.domain.user.model.dto.response.UserProfileResponse;
import com.teamproject.sellog.domain.user.model.entity.user.AccountStatus;
import com.teamproject.sellog.domain.user.model.entity.user.User;
import com.teamproject.sellog.domain.user.model.entity.user.UserPrivate;
import com.teamproject.sellog.domain.user.model.entity.user.UserProfile;
import com.teamproject.sellog.domain.user.repository.UserRepository;
import com.teamproject.sellog.domain.user.service.event.UserUpdatedEvent;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Pageable;

@Service
public class UserInfoService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final UserInfoMapper userInfoMapper;
    private final ApplicationEventPublisher eventPublisher;

    public UserInfoService(UserRepository userRepository, UserInfoMapper userInfoMapper,
            ApplicationEventPublisher eventPublisher, PostRepository postRepository) {
        this.userRepository = userRepository;
        this.userInfoMapper = userInfoMapper;
        this.eventPublisher = eventPublisher;
        this.postRepository = postRepository;
    }

    @Transactional(readOnly = true)
    public UserProfileResponse findUserProfile(String userId, String selfId, String type, Timestamp lastCreateAt,
            UUID lastId, int limit) {
        User user = userRepository.findUserWithProfileAndPrivateByUserId(userId) // 검색 대상 유저 탐색
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        UserProfile userProfile = null;
        UserPrivate userPrivate = null;
        UserContentCount userContentCount = null;
        String message = "";
        if (CheckStatus.checkSelf(userId, selfId)) { // 본인꺼 탐색하는지 확인
            userProfile = user.getUserProfile();
            userPrivate = user.getUserPrivate();
            userContentCount = userRepository.findContentCountByUserId(userId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        } else {
            User self = userRepository.findUserWithProfileAndPrivateByUserId(selfId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND)); // 본인꺼 보는게 아니니, 대상 유저데이터 탐색
            if (CheckStatus.isBlocking(user, self)) { // 차단 당한 상태인지 체크
                message = "해당 사용자에게 차단된 상태입니다.";
            } else if (CheckStatus.isPrivate(user)) { // 대상이 비공개 계정인지 체크
                if (CheckStatus.isFollowing(user, self)) {
                    userProfile = user.getUserProfile();
                    userPrivate = user.getUserPrivate();
                    userContentCount = userRepository.findContentCountByUserId(userId)
                            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
                } else {
                    message = "이 계정은 비공개 상태입니다.";
                }
            } else if (CheckStatus.checkStatus(user) == AccountStatus.INACTIVE) { // 비활성 (정지상태) 체크
                message = "이 계정은 비활성 상태입니다.";
            } else { // 차단,비공개,비활성도 아니면 그냥 보내기.
                userProfile = user.getUserProfile();
                userPrivate = user.getUserPrivate();
                userContentCount = userRepository.findContentCountByUserId(userId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
            }
        }
        if (userProfile != null && userPrivate != null && userContentCount != null) {
            CursorPageResponse<SimplePostList> postList = getPostLists(user, type, lastCreateAt, lastId, limit);
            return userInfoMapper.EntityToResponse(userProfile, userPrivate, user, userContentCount, postList);
        }
        return UserProfileResponse.builder().profileMessage(message).build();
    }

    @Transactional
    public void editUserProfile(String userId, UserProfileRequest userProfileRequest) {
        User user = userRepository.findUserWithProfileAndPrivateByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        UserProfile userProfile = user.getUserProfile();
        UserPrivate userPrivate = user.getUserPrivate();
        if (user.getAccountStatus() == AccountStatus.STAY) {
            user.setAccountStatus(AccountStatus.ACTIVE);
        }
        eventPublisher.publishEvent(new UserUpdatedEvent(this, user));
        userInfoMapper.updatePrivateFromRequest(userProfileRequest, userPrivate);
        userInfoMapper.updateProfileFromRequest(userProfileRequest, userProfile);
        userInfoMapper.updateUserFromRequest(userProfileRequest, user);
    }

    @Transactional(readOnly = true)
    public UserPreviewResponse findUserPreview(String userId, String type, Timestamp lastCreateAt, UUID lastId,
            int limit) {
        UserProfile userProfile = null;
        User user = userRepository.findUserWithProfileAndPrivateByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (CheckStatus.isPrivate(user)) {
            userProfile = new UserProfile();
            userProfile.setNickname(user.getUserProfile().getNickname());
            userProfile.setProfileMessage("이 계정은 비공개 상태입니다.");
            userProfile.setProfileThumbURL(user.getUserProfile().getProfileThumbURL());
            userProfile.setProfileURL(user.getUserProfile().getProfileURL());
            userProfile.setScore(null);
        } else if (CheckStatus.checkStatus(user) == AccountStatus.INACTIVE) {
            userProfile = new UserProfile();
            userProfile.setNickname(user.getUserProfile().getNickname());
            userProfile.setProfileMessage("이 계정은 비활성 상태입니다.");
            userProfile.setProfileThumbURL(user.getUserProfile().getProfileThumbURL());
            userProfile.setProfileURL(user.getUserProfile().getProfileURL());
            userProfile.setScore(null);
        } else {
            userProfile = user.getUserProfile();
        }

        UserContentCount userContentCount = userRepository.findContentCountByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        CursorPageResponse<SimplePostList> postList = getPostLists(user, type, lastCreateAt, lastId, limit);
        return userInfoMapper.EntityToResponse(userProfile, userContentCount, postList);
    }

    private CursorPageResponse<SimplePostList> getPostLists(User user, String type, Timestamp lastCreateAt, UUID lastId,
            int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createAt", "id"));
        PostType postType = type.equals("POST") ? PostType.POST : PostType.PRODUCT;
        List<SimplePostList> posts;
        if (lastCreateAt == null && lastId == null) {
            posts = postRepository.findAllByAuthorAndPostType(user, postType, pageable);
        } else {
            posts = postRepository.findAllByAuthorAndPostTypeAndCursor(user, postType, lastCreateAt, lastId, pageable);
        }

        boolean hasNext = posts.size() == limit;
        Timestamp nextCreateAt = null;
        UUID nextId = null;
        if (hasNext) {
            SimplePostList lastPost = posts.get(posts.size() - 1);
            nextCreateAt = lastPost.getCreateAt();
            nextId = lastPost.getPostId();
        }
        return new CursorPageResponse<>(posts, hasNext, nextCreateAt, nextId);

    }
}