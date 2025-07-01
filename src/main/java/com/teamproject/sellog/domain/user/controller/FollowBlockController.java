package com.teamproject.sellog.domain.user.controller;

import java.sql.Timestamp;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.teamproject.sellog.common.CursorPageResponse;
import com.teamproject.sellog.common.RestResponse;
import com.teamproject.sellog.domain.user.model.dto.request.OtherUserIdRequest;
import com.teamproject.sellog.domain.user.model.dto.response.BlockResponse;
import com.teamproject.sellog.domain.user.model.dto.response.FollowerResponse;
import com.teamproject.sellog.domain.user.service.FollowBlockService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FollowBlockController {
    private final FollowBlockService followBlockService;

    @GetMapping("/followers") // 팔로워 목록(페이징)
    public ResponseEntity<?> listFollower(@RequestParam(required = true) String userId,
            @RequestParam(required = false) Timestamp lastCreateAt,
            @RequestParam(required = false) UUID lastId,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            CursorPageResponse<FollowerResponse> response = followBlockService.listFollower(userId, lastCreateAt,
                    lastId,
                    limit);
            return ResponseEntity.ok(new RestResponse<>(true, "200", "follower list", response));
        } catch (Exception e) {
            return ResponseEntity.ok(new RestResponse<>(false, "404", "fatech failed by" + e.getMessage(), null));
        }

    }

    @GetMapping("/blocks") // 차단목록
    public ResponseEntity<?> listBlock(HttpServletRequest request,
            @RequestParam(required = false) Timestamp lastCreateAt,
            @RequestParam(required = false) UUID lastId,
            @RequestParam(defaultValue = "10") int limit) {
        String userId = request.getAttribute("authenticatedUserId").toString();
        try {
            CursorPageResponse<BlockResponse> response = followBlockService.listBlock(userId, lastCreateAt,
                    lastId,
                    limit);
            return ResponseEntity.ok(new RestResponse<>(true, "200", "blocks list", response));
        } catch (Exception e) {
            return ResponseEntity.ok(new RestResponse<>(false, "404", "fatech failed by" + e.getMessage(), null));
        }
    }

    @PutMapping("/following")
    public ResponseEntity<?> addFollower(HttpServletRequest request,
            @RequestBody OtherUserIdRequest otherUserIdRequest) {
        String userId = request.getAttribute("authenticatedUserId").toString();
        String otherId = otherUserIdRequest.getUserId();
        try {
            CursorPageResponse<FollowerResponse> response = followBlockService.addFollower(userId, otherId);

            return ResponseEntity.ok(new RestResponse<>(true, "200", "add success", response));
        } catch (Exception e) {
            return ResponseEntity.ok(new RestResponse<>(false, "404", "add failed by" + e.getMessage(), null));
        }
    }

    @PutMapping("/blocking")
    public ResponseEntity<?> addBlock(HttpServletRequest request,
            @RequestBody OtherUserIdRequest otherUserIdRequest) {
        String userId = request.getAttribute("authenticatedUserId").toString();
        String otherId = otherUserIdRequest.getUserId();
        try {
            CursorPageResponse<BlockResponse> response = followBlockService.addBlock(userId, otherId);
            return ResponseEntity.ok(new RestResponse<>(true, "200", "add success", response));
        } catch (Exception e) {
            return ResponseEntity.ok(new RestResponse<>(false, "404", "add failed by" + e.getMessage(), null));
        }
    }

    @PatchMapping("/unfollow")
    public ResponseEntity<?> removeFollower(HttpServletRequest request,
            @RequestBody OtherUserIdRequest otherUserIdRequest) {
        String userId = request.getAttribute("authenticatedUserId").toString();
        String otherId = otherUserIdRequest.getUserId();
        try {
            CursorPageResponse<FollowerResponse> response = followBlockService.removeFollower(userId, otherId);

            return ResponseEntity.ok(new RestResponse<>(true, "200", "remove success", response));
        } catch (Exception e) {
            return ResponseEntity.ok(new RestResponse<>(false, "404", "add failed by" + e.getMessage(), null));
        }
    }

    @PatchMapping("/unblock")
    public ResponseEntity<?> removeBlock(HttpServletRequest request,
            @RequestBody OtherUserIdRequest otherUserIdRequest) {
        String userId = request.getAttribute("authenticatedUserId").toString();
        String otherId = otherUserIdRequest.getUserId();
        try {
            CursorPageResponse<BlockResponse> response = followBlockService.removeBlock(userId, otherId);
            return ResponseEntity.ok(new RestResponse<>(true, "200", "remove success", response));
        } catch (Exception e) {
            return ResponseEntity.ok(new RestResponse<>(false, "404", "add failed by" + e.getMessage(), null));
        }
    }

}
