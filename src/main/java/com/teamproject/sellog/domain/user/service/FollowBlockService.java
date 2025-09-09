package com.teamproject.sellog.domain.user.service;

import java.sql.Timestamp;
import java.util.UUID;
import org.springframework.stereotype.Service;
import com.teamproject.sellog.common.responseUtils.CursorPageResponse;
import com.teamproject.sellog.domain.user.model.dto.response.BlockResponse;
import com.teamproject.sellog.domain.user.model.dto.response.FollowerResponse;

@Service
public interface FollowBlockService {
    CursorPageResponse<FollowerResponse> listFollower(String userId, Timestamp lastCreateAt, UUID lastId,
            int limit);

    CursorPageResponse<BlockResponse> listBlock(String userId, Timestamp lastCreateAt, UUID lastId,
            int limit);

    String addFollower(String userId, String otherId);

    CursorPageResponse<BlockResponse> addBlock(String userId, String otherId);

    CursorPageResponse<FollowerResponse> removeFollower(String userId, String otherId);

    CursorPageResponse<BlockResponse> removeBlock(String userId, String otherId);

    void acceptFollowRequest(String userId, UUID requestId);

    void declineFollowRequest(UUID requestId);
}
