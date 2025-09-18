package com.teamproject.sellog.domain.user.controller;

import java.sql.Timestamp;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.teamproject.sellog.common.responseUtils.CursorPageResponse;
import com.teamproject.sellog.common.responseUtils.RestResponse;
import com.teamproject.sellog.domain.user.model.dto.request.OtherUserIdRequest;
import com.teamproject.sellog.domain.user.model.dto.response.BlockResponse;
import com.teamproject.sellog.domain.user.model.dto.response.FollowRequestResponse;
import com.teamproject.sellog.domain.user.model.dto.response.FollowerResponse;
import com.teamproject.sellog.domain.user.service.FollowBlockService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import com.teamproject.sellog.common.responseUtils.BusinessException;
import com.teamproject.sellog.common.responseUtils.ErrorCode;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "팔로우/블락", description = "친추목록 관련 api")
public class FollowBlockController {
    private final FollowBlockService followBlockService;

    @GetMapping("/followers") // 팔로워 목록(페이징)
    @Operation(summary = "팔로워목록(*)", description = "팔로워 목록 출력")
    public ResponseEntity<?> listFollower(@RequestParam(required = true) String userId,
            @RequestParam(required = false) Timestamp lastCreateAt,
            @RequestParam(required = false) UUID lastId,
            @RequestParam(defaultValue = "10") int limit) {

        CursorPageResponse<FollowerResponse> response = followBlockService.listFollower(userId, lastCreateAt,
                lastId,
                limit);
        return ResponseEntity.ok(RestResponse.success("follower list", response));
    }

    @GetMapping("/blocks") // 차단목록
    @Operation(summary = "차단목록(*)", description = "차단 목록 출력")
    public ResponseEntity<?> listBlock(HttpServletRequest request,
            @RequestParam(required = false) Timestamp lastCreateAt,
            @RequestParam(required = false) UUID lastId,
            @RequestParam(defaultValue = "10") int limit) {
        String userId = request.getAttribute("authenticatedUserId").toString();

        CursorPageResponse<BlockResponse> response = followBlockService.listBlock(userId, lastCreateAt,
                lastId,
                limit);
        return ResponseEntity.ok(RestResponse.success("blocks list", response));
    }

    @PostMapping("/followers")
    @Operation(summary = "팔로우추가(*)", description = "팔로우 목록에 추가")
    public ResponseEntity<?> addFollower(HttpServletRequest request,
            @RequestBody OtherUserIdRequest otherUserIdRequest) {
        String userId = request.getAttribute("authenticatedUserId").toString();
        String otherId = otherUserIdRequest.getOtherId();

        String message = followBlockService.addFollower(userId, otherId);

        return ResponseEntity.ok(RestResponse.success(message, null));

    }

    @PostMapping("/blocks")
    @Operation(summary = "차단(*)", description = "차단 목록에 추가")
    public ResponseEntity<?> addBlock(HttpServletRequest request,
            @RequestBody OtherUserIdRequest otherUserIdRequest) {
        String userId = request.getAttribute("authenticatedUserId").toString();
        String otherId = otherUserIdRequest.getOtherId();

        CursorPageResponse<BlockResponse> response = followBlockService.addBlock(userId, otherId);
        return ResponseEntity.ok(RestResponse.success("add success", response));

    }

    @DeleteMapping("/followers/{otherId}")
    @Operation(summary = "팔로우 해제(*)", description = "팔로우 목록에서 삭제")
    public ResponseEntity<?> removeFollower(HttpServletRequest request,
            @PathVariable String otherId) {
        String userId = request.getAttribute("authenticatedUserId").toString();

        CursorPageResponse<FollowerResponse> response = followBlockService.removeFollower(userId, otherId);

        return ResponseEntity.ok(RestResponse.success("remove success", response));

    }

    @DeleteMapping("/blocks/{otherId}")
    @Operation(summary = "차단 해제(*)", description = "차단 목록에서 삭제")
    public ResponseEntity<?> removeBlock(HttpServletRequest request,
            @PathVariable String otherId) {
        String userId = request.getAttribute("authenticatedUserId").toString();
        CursorPageResponse<BlockResponse> response = followBlockService.removeBlock(userId, otherId);
        return ResponseEntity.ok(RestResponse.success("remove success", response));

    }

    @PostMapping("/followers/accept/{requestId}")
    @Operation(summary = "팔로우 요청 수락(+)", description = "받은 팔로우 요청을 수락합니다.")
    public ResponseEntity<?> acceptFollowRequest(HttpServletRequest request, @PathVariable UUID requestId) {
        String userId = request.getAttribute("authenticatedUserId").toString();
        followBlockService.acceptFollowRequest(userId, requestId);
        return ResponseEntity.ok(RestResponse.success("팔로우 요청을 수락했습니다.", null));
    }

    @DeleteMapping("/followers/decline/{requestId}")
    @Operation(summary = "팔로우 요청 거절(+)", description = "받은 팔로우 요청을 거절합니다.")
    public ResponseEntity<?> declineFollowRequest(HttpServletRequest request, @PathVariable UUID requestId) {
        // 본인에게 온 요청인지 확인하는 로직을 서비스에 추가할 수 있으나,
        // 여기서는 요청 ID만으로 삭제 처리
        followBlockService.declineFollowRequest(requestId);
        return ResponseEntity.ok(RestResponse.success("팔로우 요청을 거절했습니다.", null));
    }

    @GetMapping("/followers/requests")
    @Operation(summary = "팔로우 요청 목록 조회(+)", description = "받은 팔로우 요청 목록을 조회합니다.")
    public ResponseEntity<RestResponse<CursorPageResponse<FollowRequestResponse>>> listFollowRequests(
            HttpServletRequest request,
            @RequestParam(required = false) Timestamp lastCreateAt,
            @RequestParam(required = false) UUID lastId,
            @RequestParam(defaultValue = "10") int limit) {
        String userId = request.getAttribute("authenticatedUserId").toString();
        CursorPageResponse<FollowRequestResponse> responseData = followBlockService.listFollowRequests(userId,
                lastCreateAt, lastId, limit);
        return ResponseEntity.ok(RestResponse.success("팔로우 요청 목록을 성공적으로 조회했습니다.", responseData));
    }
}
