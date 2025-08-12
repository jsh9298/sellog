package com.teamproject.sellog.domain.user.service;

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
import com.teamproject.sellog.domain.user.model.dto.response.FollowerResponse;
import com.teamproject.sellog.domain.user.model.entity.friend.Block;
import com.teamproject.sellog.domain.user.model.entity.friend.Follow;
import com.teamproject.sellog.domain.user.model.entity.user.User;
import com.teamproject.sellog.domain.user.repository.BlockRepository;
import com.teamproject.sellog.domain.user.repository.FollowRepository;
import com.teamproject.sellog.domain.user.repository.UserRepository;

@Service
public class FollowBlockService {
    private final UserRepository userRepository;
    private final FollowBlockMapper followBlockMapper;
    private final FollowRepository followRepository;
    private final BlockRepository blockRepository;

    public FollowBlockService(final UserRepository userRepository, final FollowBlockMapper followBlockMapper,
            final FollowRepository followRepository, final BlockRepository blockRepository) {
        this.userRepository = userRepository;
        this.followBlockMapper = followBlockMapper;
        this.followRepository = followRepository;
        this.blockRepository = blockRepository;
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

        return new CursorPageResponse<>(followDto, hasNext, nextCreateAt, nextId);
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
        return new CursorPageResponse<>(blockDto, hasNext, nextCreateAt, nextId);
    }

    @Transactional
    public CursorPageResponse<FollowerResponse> addFollower(String userId, String otherId) {
        User user = findUser(userId);
        User other = findUser(otherId);
        boolean added = user.addFollowing(other);
        if (!added) {
            throw new BusinessException(ErrorCode.INVALID_F_LIST_CONTROL);
        }
        return listFollower(userId, null, null, 10);
    }

    @Transactional
    public CursorPageResponse<BlockResponse> addBlock(String userId, String otherId) {
        User user = findUser(userId);
        User other = findUser(otherId);
        boolean added = user.addBlocking(other);
        if (!added) {
            throw new BusinessException(ErrorCode.INVALID_F_LIST_CONTROL);
        }
        return listBlock(userId, null, null, 10);
    }

    @Transactional
    public CursorPageResponse<FollowerResponse> removeFollower(String userId, String otherId) {
        User user = findUser(userId);
        User other = findUser(otherId);
        boolean removed = user.removeFollowing(other);
        if (!removed) {
            throw new BusinessException(ErrorCode.INVALID_F_LIST_CONTROL);
        }
        return listFollower(userId, null, null, 10);
    }

    @Transactional
    public CursorPageResponse<BlockResponse> removeBlock(String userId, String otherId) {
        User user = findUser(userId);
        User other = findUser(otherId);
        boolean removed = user.removeBlocking(other);
        if (!removed) {
            throw new BusinessException(ErrorCode.INVALID_F_LIST_CONTROL);
        }
        return listBlock(userId, null, null, 10);
    }

    private User findUser(String userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private UUID userIdToId(String userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND)).getId();
    }

}
