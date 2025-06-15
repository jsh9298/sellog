package com.teamproject.sellog.auth.controller;

import com.teamproject.sellog.auth.jwt.JWT;
import com.teamproject.sellog.auth.jwt.JwtProvider;
import com.teamproject.sellog.auth.model.RefreshtokenDto;
import com.teamproject.sellog.auth.model.UserDeletDto;
import com.teamproject.sellog.auth.model.UserLoginDto;
import com.teamproject.sellog.auth.model.UserRegisterDto;
import com.teamproject.sellog.auth.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor; // final 필드를 인자로 받는 생성자 생성
import lombok.Setter;

import org.springframework.http.HttpStatus;
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
            return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
        } catch (IllegalArgumentException e) {
            // 사용자 ID 또는 이메일 중복 등 예외 처리
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (RuntimeException e) {
            // 비밀번호 해싱 오류 등 기타 예외 처리
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Registration failed");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginDto userLoginDto) {
        try {
            JWT jwtTokens = authService.loginUser(userLoginDto);

            Map<String, String> response = new HashMap<>();
            response.put("accessToken", jwtTokens.getAccessToken());
            response.put("refreshToken", jwtTokens.getRefreshToken());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Login failed");
        }
    }

    // 로그아웃 엔드포인트
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {

        String accessToken = resolveToken(request); // 요청 헤더에서 액세스 토큰 추출
        String refreshToken = request.getHeader("X-Refresh-Token"); // 예시: 리프레시 토큰은 별도 헤더에서 추출

        if (accessToken == null && refreshToken == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tokens not provided");
        }

        try {
            authService.logoutUser(accessToken, refreshToken);
            return ResponseEntity.ok("Logout successful");
        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Logout failed");
        }
    }

    // 회원 탈퇴 엔드포인트
    @DeleteMapping("/delete") // DELETE 메소드 사용 권장
    public ResponseEntity<?> deleteUser(@RequestBody UserDeletDto userDeletDto, HttpServletRequest request) {
        String accessToken = resolveToken(request); // 요청 헤더에서 액세스 토큰 추출
        String refreshToken = request.getHeader("X-Refresh-Token"); // 예시: 리프레시 토큰은 별도 헤더에서 추출

        if (accessToken == null || refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication tokens required");
        }

        // JWT 필터에서 인증된 사용자 ID를 가져옵니다.
        String authenticatedUserId = (String) request.getAttribute("authenticatedUserId");
        if (authenticatedUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        // 탈퇴 요청의 사용자 ID와 인증된 사용자 ID가 일치하는지 확인
        if (!authenticatedUserId.equals(userDeletDto.getUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Cannot delete other user's account");
        }

        try {
            authService.deleteUser(userDeletDto.getUserId(), userDeletDto.getPassword(), accessToken, refreshToken);
            return ResponseEntity.ok("User deleted successfully");
        } catch (IllegalArgumentException e) {
            // 사용자 없음, 비밀번호 불일치 등 예외 처리
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (RuntimeException e) {
            // 기타 삭제 처리 중 오류 발생 시
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("User deletion failed");
        }
    }

    // 토큰 갱신 엔드포인트 (리프레시 토큰 기반)
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshtokenDto refreshtokenDto) {
        try {
            // AuthService의 토큰 갱신 로직 호출
            JWT newTokens = authService.refreshToken(refreshtokenDto.getRefreshToken());

            Map<String, String> response = new HashMap<>();
            response.put("accessToken", newTokens.getAccessToken());
            response.put("refreshToken", newTokens.getRefreshToken());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            // 유효하지 않거나 블랙리스트에 있는 리프레시 토큰 등의 경우
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (RuntimeException e) {
            // 기타 토큰 갱신 처리 중 오류 발생 시
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Token refresh failed");
        }
    }

    // 요청 헤더에서 JWT 토큰 추출 ("Bearer " 접두사 제거) - 중복 코드 방지를 위해 유틸리티 클래스로 분리 가능
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
