package com.teamproject.sellog.auth.controller;

import com.teamproject.sellog.auth.model.dto.request.CheckIdDto;
import com.teamproject.sellog.auth.model.dto.request.UserDeleteDto;
import com.teamproject.sellog.auth.model.dto.request.UserFindIdDto;
import com.teamproject.sellog.auth.model.dto.request.UserLoginDto;
import com.teamproject.sellog.auth.model.dto.request.UserPasswordDto;
import com.teamproject.sellog.auth.model.dto.request.UserRegisterDto;
import com.teamproject.sellog.auth.model.dto.response.UserLoginResponse;
import com.teamproject.sellog.auth.model.jwt.JWT;
import com.teamproject.sellog.auth.service.AuthService;
import com.teamproject.sellog.common.RestResponse;
import com.teamproject.sellog.common.TokenExtractor;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService; // AuthService 주입

    private final long refreshTokenValidityInMilliseconds; // 리프레시 토큰 유효 기간
    // @RequiredArgsConstructor는 @Value에 해당하는 값을 인식못함. 직접 주입해줘야함.

    public AuthController(final AuthService authService,
            @Value("${jwt.refresh-token-expiration-ms}") final long refreshTokenValidityInMilliseconds) {
        this.authService = authService;
        this.refreshTokenValidityInMilliseconds = refreshTokenValidityInMilliseconds;
    }

    @PostMapping("/checkId")
    public ResponseEntity<?> checkId(@RequestBody CheckIdDto checkIdDto) {
        if (!authService.checkId(checkIdDto.getUserId())) {
            return ResponseEntity.ok(new RestResponse<>(true, "200", "You can use this Id", null));
        } else {
            return ResponseEntity.ok(new RestResponse<>(false, "500", "This Id Already exist", null));
        }

    }

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
            UserLoginResponse jwtTokens = authService.loginUser(userLoginDto);

            String accessToken = jwtTokens.getAccessToken();
            String profileThumbURL = jwtTokens.getProfileThumbURL();
            ResponseCookie cookie = ResponseCookie.from("refreshToken", jwtTokens.getRefreshToken())
                    .maxAge(refreshTokenValidityInMilliseconds)
                    .path("/")
                    .secure(true)
                    .sameSite("None")
                    .httpOnly(true)
                    .build();
            Map<String, String> responseMap = new HashMap<>();
            responseMap.put("accessToken", accessToken);
            responseMap.put("profileThumbURL", profileThumbURL);
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(new RestResponse<>(true, "200", "Login Success", responseMap));
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
        String refreshToken = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) { // 쿠키 이름이 "refreshToken"인지 확인
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        if (accessToken == null && (refreshToken == null || refreshToken.isEmpty())) {
            return ResponseEntity.ok(new RestResponse<>(false, "400", "Tokens not provided", null));
        }

        try {
            authService.logoutUser(accessToken, refreshToken);
            ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                    .maxAge(0)
                    .path("/")
                    .secure(true)
                    .sameSite("None")
                    .httpOnly(true)
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString()) // Set-Cookie 헤더 추가
                    .body(new RestResponse<>(true, "200", "Logout successful", null));
        } catch (Exception e) {
            return ResponseEntity.ok(new RestResponse<>(false, "500", "Logout failed", null));
        }
    }

    // 회원 탈퇴 엔드포인트
    @DeleteMapping("/delete") // DELETE 메소드 사용 권장
    public ResponseEntity<?> deleteUser(@RequestBody UserDeleteDto userDeleteDto, HttpServletRequest request) {
        String accessToken = TokenExtractor.extractTokenFromHeader(request); // 요청 헤더에서 액세스 토큰 추출
        String refreshToken = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) { // 쿠키 이름이 "refreshToken"인지 확인
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }
        if (accessToken == null || (refreshToken == null || refreshToken.isEmpty())) {
            return ResponseEntity.ok(new RestResponse<>(false, "401", "Authentication tokens required", null));
        }

        // JWT 필터에서 인증된 사용자 ID를 가져옵니다.
        String authenticatedUserId = (String) request.getAttribute("authenticatedUserId");
        if (authenticatedUserId == null) {
            return ResponseEntity.ok(new RestResponse<>(false, "401", "User not authenticated", null));
        }

        // 탈퇴 요청의 사용자 ID와 인증된 사용자 ID가 일치하는지 확인
        if (!authenticatedUserId.equals(userDeleteDto.getUserId())) {
            return ResponseEntity.ok(new RestResponse<>(false, "403", "Cannot delete other user's account", null));
        }

        try {
            authService.deleteUser(userDeleteDto.getUserId(), userDeleteDto.getPassword(), accessToken, refreshToken);
            ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                    .maxAge(0)
                    .path("/")
                    .secure(true)
                    .sameSite("None")
                    .httpOnly(true)
                    .build();

            return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(new RestResponse<>(true, "200", "User deleted successfully", null));
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
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        try {
            String refreshToken = "";
            // AuthService의 토큰 갱신 로직 호출
            if (request.getCookies() != null) {
                for (Cookie cookie : request.getCookies()) {
                    if ("refreshToken".equals(cookie.getName())) { // 쿠키 이름이 "refreshToken"인지 확인
                        refreshToken = cookie.getValue();
                    }
                }
            }

            JWT jwtTokens = authService.refreshToken(refreshToken);

            String accessToken = jwtTokens.getAccessToken();

            ResponseCookie cookie = ResponseCookie.from("refreshToken", jwtTokens.getRefreshToken())
                    .maxAge(refreshTokenValidityInMilliseconds)
                    .path("/")
                    .secure(true)
                    .sameSite("None")
                    .httpOnly(true)
                    .build();
            Map<String, String> responseMap = Collections.singletonMap("accessToken", accessToken);
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(new RestResponse<>(true, "200", "Login Success", responseMap));

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
