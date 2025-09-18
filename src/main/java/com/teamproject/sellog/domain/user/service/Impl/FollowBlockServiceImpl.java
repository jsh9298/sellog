package com.teamproject.sellog.domain.user.service.Impl;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.teamproject.sellog.common.responseUtils.BusinessException;
import com.teamproject.sellog.common.responseUtils.CursorPageResponse;
import com.teamproject.sellog.common.responseUtils.ErrorCode;
import com.teamproject.sellog.domain.user.mapper.FollowBlockMapper;
import com.teamproject.sellog.domain.user.model.dto.response.BlockResponse;
import com.teamproject.sellog.domain.user.model.dto.response.FollowRequestResponse;
import com.teamproject.sellog.domain.user.model.dto.response.FollowerResponse;
import com.teamproject.sellog.domain.user.model.entity.friend.Block;
import com.teamproject.sellog.domain.user.model.entity.friend.FollowRequest;
import com.teamproject.sellog.domain.user.model.entity.friend.Follow;
import com.teamproject.sellog.domain.user.model.entity.user.User;
import com.teamproject.sellog.common.accountsUtils.CheckStatus;
import com.teamproject.sellog.domain.user.repository.BlockRepository;
import com.teamproject.sellog.domain.user.repository.FollowRepository;
import com.teamproject.sellog.domain.user.repository.FollowRequestRepository;
import com.teamproject.sellog.domain.user.repository.UserRepository;
import com.teamproject.sellog.domain.user.service.FollowBlockService;

@Service
public class FollowBlockServiceImpl implements FollowBlockService {
    private final UserRepository userRepository;
    private final FollowBlockMapper followBlockMapper;
    private final FollowRepository followRepository;
    private final BlockRepository blockRepository;
    private final FollowRequestRepository followRequestRepository;

    public FollowBlockServiceImpl(final UserRepository userRepository, final FollowBlockMapper followBlockMapper,
            final FollowRepository followRepository, final BlockRepository blockRepository,
            final FollowRequestRepository followRequestRepository) {
        this.userRepository = userRepository;
        this.followBlockMapper = followBlockMapper;
        this.followRepository = followRepository;
        this.blockRepository = blockRepository;
        this.followRequestRepository = followRequestRepository;
    }

    @Transactional(readOnly = true)
    public CursorPageResponse<FollowerResponse> listFollower(String userId, Timestamp lastCreateAt, UUID lastId,
            int limit) {
        UUID followerId = userIdToId(userId);

        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createAt", "id"));

        List<Follow> follows;
        if (lastCreateAt == null && lastId == null) {
            follows = followRepository.findByFollowerIdOrderByCreateAtDescIdDesc(followerId, pageable);
        } else {
            follows = followRepository.findByFollowerIdAndCursor(followerId, lastCreateAt, lastId, pageable);
        }
        List<FollowerResponse> followDto = follows.stream().map(followBlockMapper::toFollowerResponse)
                .collect(Collectors.toList());

        boolean hasNext = follows.size() == limit;
        Timestamp nextCreateAt = null;
        UUID nextId = null;
        if (hasNext) {
            Follow lastFollow = follows.get(follows.size() - 1);
            nextCreateAt = lastFollow.getCreateAt();
            nextId = lastFollow.getId();
        }

        return new CursorPageResponse<>(followDto, hasNext, null, nextCreateAt, nextId);
    }

    @Transactional(readOnly = true)
    public CursorPageResponse<BlockResponse> listBlock(String userId, Timestamp lastCreateAt, UUID lastId,
            int limit) {
        UUID blockedId = userIdToId(userId);
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createAt", "id"));
        List<Block> blocks;
        if (lastCreateAt == null && lastId == null) {
            blocks = blockRepository.findByBlockingIdOrderByCreateAtDescIdDesc(blockedId, pageable);
        } else {
            blocks = blockRepository.findByBlockingIdAndCursor(blockedId, lastCreateAt, lastId, pageable);
        }
        List<BlockResponse> blockDto = blocks.stream().map(followBlockMapper::toBlockResponse)
                .collect(Collectors.toList());

        boolean hasNext = blocks.size() == limit;
        Timestamp nextCreateAt = null;
        UUID nextId = null;
        if (hasNext) {
            Block lastBlock = blocks.get(blocks.size() - 1);
            nextCreateAt = lastBlock.getCreateAt();
            nextId = lastBlock.getId();
        }
        return new CursorPageResponse<>(blockDto, hasNext, null, nextCreateAt, nextId);
    }

    @Transactional
    public String addFollower(String userId, String otherId) {
        User user = findUser(userId);
        User other = findUser(otherId);

        if (CheckStatus.isPrivate(other)) {
            // 대상이 비공개 계정이면 팔로우 요청 생성
            if (followRequestRepository.findByRequesterIdAndReceiverId(user.getId(), other.getId()).isPresent()) {
                throw new BusinessException(ErrorCode.FOLLOW_REQUEST_ALREADY_EXISTS);
            }
            FollowRequest request = new FollowRequest();
            request.setRequester(user);
            request.setTarget(other);
            followRequestRepository.save(request);
            return "팔로우 요청을 보냈습니다.";
        } else {
            // 대상이 공개 계정이면 즉시 팔로우
            User userWithRelations = findUserWithRelations(userId);
            if (!userWithRelations.addFollowing(other)) {
                throw new BusinessException(ErrorCode.INVALID_F_LIST_CONTROL);
            }
            user.getUserProfile().setFollowingCount(user.getUserProfile().getFollowingCount() + 1);
            other.getUserProfile().setFollowerCount(other.getUserProfile().getFollowerCount() + 1);
            return "팔로우를 시작했습니다.";
        }
    }

    @Transactional
    public CursorPageResponse<BlockResponse> addBlock(String userId, String otherId) {
        User user = findUserWithRelations(userId);
        User other = findUser(otherId);
        boolean added = user.addBlocking(other);
        if (!added) {
            throw new BusinessException(ErrorCode.INVALID_F_LIST_CONTROL);
        }
        return listBlock(userId, null, null, 10);
    }

    @Transactional
    public CursorPageResponse<FollowerResponse> removeFollower(String userId, String otherId) {
        User user = findUserWithRelations(userId);
        User other = findUser(otherId);
        boolean removed = user.removeFollowing(other);
        if (!removed) {
            throw new BusinessException(ErrorCode.INVALID_F_LIST_CONTROL);
        } else {
            // 언팔로우 성공 시 카운터 업데이트
            user.getUserProfile().setFollowingCount(Math.max(0, user.getUserProfile().getFollowingCount() - 1));
            other.getUserProfile().setFollowerCount(Math.max(0, other.getUserProfile().getFollowerCount() - 1));
        }
        return listFollower(userId, null, null, 10);
    }

    @Transactional
    public CursorPageResponse<BlockResponse> removeBlock(String userId, String otherId) {
        User user = findUserWithRelations(userId);
        User other = findUser(otherId);
        boolean removed = user.removeBlocking(other);
        if (!removed) {
            throw new BusinessException(ErrorCode.INVALID_F_LIST_CONTROL);
        }
        return listBlock(userId, null, null, 10);
    }

    @Transactional
    public void acceptFollowRequest(String userId, UUID requestId) {
        FollowRequest request = followRequestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FOLLOW_REQUEST_NOT_FOUND));

        // 요청을 수락하는 사용자가 요청의 대상이 맞는지 확인
        if (!request.getTarget().getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        User requester = request.getRequester();
        User target = request.getTarget();

        // Follow 엔티티 생성
        Follow newFollow = new Follow();
        newFollow.setFollower(requester);
        newFollow.setFollowed(target);
        followRepository.save(newFollow);

        // 카운터 업데이트
        requester.getUserProfile().setFollowingCount(requester.getUserProfile().getFollowingCount() + 1);
        target.getUserProfile().setFollowerCount(target.getUserProfile().getFollowerCount() + 1);

        // 처리된 요청 삭제
        followRequestRepository.delete(request);
    }

    @Transactional
    public void declineFollowRequest(UUID requestId) {
        // 요청 존재 여부만 확인하고 삭제
        followRequestRepository.deleteById(requestId);
    }

    @Override
    @Transactional(readOnly = true)
    public CursorPageResponse<FollowRequestResponse> listFollowRequests(String userId, Timestamp lastCreateAt,
            UUID lastId, int limit) {
        User user = findUser(userId);

        // 다음 페이지 조회를 위해 limit + 1 만큼 요청
        Pageable pageable = PageRequest.of(0, limit + 1, Sort.by(Sort.Direction.DESC, "createAt", "id"));

        // 첫 페이지 조회 시 커서 값을 현재 시간과 최대 UUID로 설정
        if (lastCreateAt == null) {
            lastCreateAt = new Timestamp(System.currentTimeMillis());
        }
        if (lastId == null) {
            lastId = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");
        }

        List<FollowRequest> requests = followRequestRepository.findByReceiverIdWithCursor(user.getId(), lastCreateAt,
                lastId, pageable);

        boolean hasNext = requests.size() > limit;
        List<FollowRequest> content = hasNext ? requests.subList(0, limit) : requests;

        List<FollowRequestResponse> responseContent = content.stream()
                .map(FollowRequestResponse::fromEntity)
                .collect(Collectors.toList());

        Timestamp nextCursorCreateAt = hasNext ? content.get(limit - 1).getCreateAt() : null;
        UUID nextCursorId = hasNext ? content.get(limit - 1).getId() : null;

        return new CursorPageResponse<>(responseContent, hasNext, null, nextCursorCreateAt, nextCursorId);
    }

    private User findUser(String userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private User findUserWithRelations(String userId) {
        return userRepository.findByUserIdWithRelations(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private UUID userIdToId(String userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND)).getId();
    }

}
