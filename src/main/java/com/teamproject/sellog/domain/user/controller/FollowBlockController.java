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

import com.teamproject.sellog.common.dtoUtils.CursorPageResponse;
import com.teamproject.sellog.common.dtoUtils.RestResponse;
import com.teamproject.sellog.domain.user.model.dto.request.OtherUserIdRequest;
import com.teamproject.sellog.domain.user.model.dto.response.BlockResponse;
import com.teamproject.sellog.domain.user.model.dto.response.FollowerResponse;
import com.teamproject.sellog.domain.user.service.FollowBlockService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "팔로우/블락", description = "친추목록 관련 api")
public class FollowBlockController {
    private final FollowBlockService followBlockService;

    @GetMapping("/followers") // 팔로워 목록(페이징)
    @Operation(summary = "팔로워목록", description = "팔로워 목록 출력(*)")
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
    @Operation(summary = "차단목록", description = "차단 목록 출력(*)")
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

    @PostMapping("/followers")
    @Operation(summary = "팔로우추가", description = "팔로우 목록에 추가(*)")
    public ResponseEntity<?> addFollower(HttpServletRequest request,
            @RequestBody OtherUserIdRequest otherUserIdRequest) {
        String userId = request.getAttribute("authenticatedUserId").toString();
        String otherId = otherUserIdRequest.getOtherId();
        try {
            CursorPageResponse<FollowerResponse> response = followBlockService.addFollower(userId, otherId);

            return ResponseEntity.ok(new RestResponse<>(true, "200", "add success", response));
        } catch (Exception e) {
            return ResponseEntity.ok(new RestResponse<>(false, "404", "add failed by" + e.getMessage(), null));
        }
    }

    @PostMapping("/blocks")
    @Operation(summary = "차단", description = "차단 목록에 추가(*)")
    public ResponseEntity<?> addBlock(HttpServletRequest request,
            @RequestBody OtherUserIdRequest otherUserIdRequest) {
        String userId = request.getAttribute("authenticatedUserId").toString();
        String otherId = otherUserIdRequest.getOtherId();
        try {
            CursorPageResponse<BlockResponse> response = followBlockService.addBlock(userId, otherId);
            return ResponseEntity.ok(new RestResponse<>(true, "200", "add success", response));
        } catch (Exception e) {
            return ResponseEntity.ok(new RestResponse<>(false, "404", "add failed by" + e.getMessage(), null));
        }
    }

    @DeleteMapping("/followers/{otherId}")
    @Operation(summary = "팔로우 해제", description = "팔로우 목록에서 삭제(*)")
    public ResponseEntity<?> removeFollower(HttpServletRequest request,
            @PathVariable String otherId) {
        String userId = request.getAttribute("authenticatedUserId").toString();

        try {
            CursorPageResponse<FollowerResponse> response = followBlockService.removeFollower(userId, otherId);

            return ResponseEntity.ok(new RestResponse<>(true, "200", "remove success", response));
        } catch (Exception e) {
            return ResponseEntity.ok(new RestResponse<>(false, "404", "add failed by" + e.getMessage(), null));
        }
    }

    @DeleteMapping("/blocks/{otherId}")
    @Operation(summary = "차단 해제", description = "차단 목록에서 삭제(*)")
    public ResponseEntity<?> removeBlock(HttpServletRequest request,
            @PathVariable String otherId) {
        String userId = request.getAttribute("authenticatedUserId").toString();

        try {
            CursorPageResponse<BlockResponse> response = followBlockService.removeBlock(userId, otherId);
            return ResponseEntity.ok(new RestResponse<>(true, "200", "remove success", response));
        } catch (Exception e) {
            return ResponseEntity.ok(new RestResponse<>(false, "404", "add failed by" + e.getMessage(), null));
        }
    }

}
