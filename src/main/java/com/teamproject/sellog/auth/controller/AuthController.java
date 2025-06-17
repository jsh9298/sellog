package com.teamproject.sellog.auth.controller;

import com.teamproject.sellog.auth.model.DTO.request.RefreshtokenDto;
import com.teamproject.sellog.auth.model.DTO.request.UserDeletDto;
import com.teamproject.sellog.auth.model.DTO.request.UserFindIdDto;
import com.teamproject.sellog.auth.model.DTO.request.UserLoginDto;
import com.teamproject.sellog.auth.model.DTO.request.UserPasswordDto;
import com.teamproject.sellog.auth.model.DTO.request.UserRegisterDto;
import com.teamproject.sellog.auth.model.jwt.JWT;
import com.teamproject.sellog.auth.service.AuthService;
import com.teamproject.sellog.common.RestResponse;
import com.teamproject.sellog.common.TokenExtractor;

import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor; // final 필드를 인자로 받는 생성자 생성

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService; // AuthService 주입

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRegisterDto UserRegisterDto) {
        try {
            authService.registerUser(UserRegisterDto);
            return ResponseEntity.ok(new RestResponse<>(true, "200", "User registered successfully", null));
        } catch (IllegalArgumentException e) {
            // 사용자 ID 또는 이메일 중복 등 예외 처리
            return ResponseEntity.ok(new RestResponse<>(false, "400", e.getMessage(), null));
        } catch (RuntimeException e) {
            // 비밀번호 해싱 오류 등 기타 예외 처리 //수정 필요.
            return ResponseEntity.ok(new RestResponse<>(false, "500", e.getMessage(), null));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginDto userLoginDto) {
        try {
            JWT jwtTokens = authService.loginUser(userLoginDto);

            Map<String, String> response = new HashMap<>();
            response.put("accessToken", jwtTokens.getAccessToken());
            response.put("refreshToken", jwtTokens.getRefreshToken());
            return ResponseEntity.ok(new RestResponse<Map<String, String>>(true, "200", "Login Success", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(new RestResponse<>(false, "401", e.getMessage(), null));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(new RestResponse<>(false, "500", "Login failed", null));
        }
    }

    // 로그아웃 엔드포인트
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {

        String accessToken = TokenExtractor.extractTokenFromHeader(request); // 요청 헤더에서 액세스 토큰 추출
        String refreshToken = request.getHeader("X-Refresh-Token"); // 리프레시 토큰은 별도 헤더에서 추출

        if (accessToken == null && refreshToken == null) {
            return ResponseEntity.ok(new RestResponse<>(false, "400", "Tokens not provided", null));
        }

        try {
            authService.logoutUser(accessToken, refreshToken);
            return ResponseEntity.ok(new RestResponse<>(true, "200", "Logout successful", null));
        } catch (Exception e) {
            return ResponseEntity.ok(new RestResponse<>(false, "500", "Logout failed", null));
        }
    }

    // 회원 탈퇴 엔드포인트
    @DeleteMapping("/delete") // DELETE 메소드 사용 권장
    public ResponseEntity<?> deleteUser(@RequestBody UserDeletDto userDeletDto, HttpServletRequest request) {
        String accessToken = TokenExtractor.extractTokenFromHeader(request); // 요청 헤더에서 액세스 토큰 추출
        String refreshToken = request.getHeader("X-Refresh-Token"); // 리프레시 토큰은 별도 헤더에서 추출

        if (accessToken == null || refreshToken == null) {
            return ResponseEntity.ok(new RestResponse<>(false, "401", "Authentication tokens required", null));
        }

        // JWT 필터에서 인증된 사용자 ID를 가져옵니다.
        String authenticatedUserId = (String) request.getAttribute("authenticatedUserId");
        if (authenticatedUserId == null) {
            return ResponseEntity.ok(new RestResponse<>(false, "401", "User not authenticated", null));
        }

        // 탈퇴 요청의 사용자 ID와 인증된 사용자 ID가 일치하는지 확인
        if (!authenticatedUserId.equals(userDeletDto.getUserId())) {
            return ResponseEntity.ok(new RestResponse<>(false, "403", "Cannot delete other user's account", null));
        }

        try {
            authService.deleteUser(userDeletDto.getUserId(), userDeletDto.getPassword(), accessToken, refreshToken);
            return ResponseEntity.ok(new RestResponse<>(true, "200", "User deleted successfully", null));
        } catch (IllegalArgumentException e) {
            // 사용자 없음, 비밀번호 불일치 등 예외 처리
            return ResponseEntity.ok(new RestResponse<>(false, "400", e.getMessage(), null));
        } catch (RuntimeException e) {
            // 기타 삭제 처리 중 오류 발생 시
            return ResponseEntity.ok(new RestResponse<>(false, "500", "User deletion failed", null));
        }
    }

    // 토큰 갱신 엔드포인트 (리프레시 토큰 기반) //수정 필요.
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshtokenDto refreshtokenDto) {
        try {
            // AuthService의 토큰 갱신 로직 호출
            JWT newTokens = authService.refreshToken(refreshtokenDto.getRefreshToken());

            Map<String, String> response = new HashMap<>();
            response.put("accessToken", newTokens.getAccessToken());
            response.put("refreshToken", newTokens.getRefreshToken());
            return ResponseEntity.ok(new RestResponse<Map<String, String>>(true, "200", "Token refresh", response));
        } catch (IllegalArgumentException e) {
            // 유효하지 않거나 블랙리스트에 있는 리프레시 토큰 등의 경우
            return ResponseEntity.ok(new RestResponse<>(false, "401", e.getMessage(), null));
        } catch (RuntimeException e) {
            // 기타 토큰 갱신 처리 중 오류 발생 시
            return ResponseEntity.ok(new RestResponse<>(false, "500", "Token refresh failed", null));
        }
    }

    // 아이디 검색 엔드포인트
    @PostMapping("/find")
    public ResponseEntity<?> findUserId(@RequestBody UserFindIdDto userFindIdDto) {
        try {
            String userId = authService.findUserId(userFindIdDto.getEmail());
            return ResponseEntity.ok(new RestResponse<String>(true, "200", "Find user's id successfully", userId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(new RestResponse<>(false, "404", e.getMessage(), null));
        }
    }

    // 비밀번호 변경 엔드포인트, 로그인 전 비밀번호 변경을 위해 존재하기 때문에, email과 변경할 password를 받음.

    /* 이메일 인증과 연동되어야함. */

    @PatchMapping("/password")
    public ResponseEntity<?> changePassword(@RequestBody UserPasswordDto userPasswordDto) {
        try {
            authService.changePassword(userPasswordDto.getUserId(), userPasswordDto.getEmail(),
                    userPasswordDto.getPassword());
            return ResponseEntity.ok(new RestResponse<>(true, "200", "Password change successfully", null));
        } catch (Exception e) {
            return ResponseEntity.ok(new RestResponse<>(false, "404", e.getMessage(), null));
        }
    }

}
