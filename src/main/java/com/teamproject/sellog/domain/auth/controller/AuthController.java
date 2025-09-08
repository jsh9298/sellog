package com.teamproject.sellog.domain.auth.controller;

import com.teamproject.sellog.common.accountsUtils.TokenExtractor;
import com.teamproject.sellog.common.emailUtils.EmailService;
import com.teamproject.sellog.common.responseUtils.BusinessException;
import com.teamproject.sellog.common.responseUtils.ErrorCode;
import com.teamproject.sellog.common.responseUtils.RestResponse;
import com.teamproject.sellog.domain.auth.model.dto.request.CheckIdDto;
import com.teamproject.sellog.domain.auth.model.dto.request.UserDeleteDto;
import com.teamproject.sellog.domain.auth.model.dto.request.UserFindIdDto;
import com.teamproject.sellog.domain.auth.model.dto.request.UserLoginDto;
import com.teamproject.sellog.domain.auth.model.dto.request.UserPasswordDto;
import com.teamproject.sellog.domain.auth.model.dto.request.UserRegisterDto;
import com.teamproject.sellog.domain.auth.model.dto.response.UserLoginResponse;
import com.teamproject.sellog.domain.auth.model.jwt.JWT;
import com.teamproject.sellog.domain.auth.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/auth")
@Tag(name = "회원관련", description = "회원정보 관련 api")
public class AuthController {

    private final AuthService authService; // AuthService 주입

    private final long refreshTokenValidityInMilliseconds; // 리프레시 토큰 유효 기간
    // @RequiredArgsConstructor는 @Value에 해당하는 값을 인식못함. 직접 주입해줘야함.

    private final EmailService emailService;

    public AuthController(final AuthService authService,
            final EmailService emailService,
            @Value("${jwt.refresh-token-expiration-ms}") final long refreshTokenValidityInMilliseconds) {
        this.authService = authService;
        this.emailService = emailService;
        this.refreshTokenValidityInMilliseconds = refreshTokenValidityInMilliseconds;
    }

    @PostMapping("/checkId")
    @Operation(summary = "아이디 중복 체크(*)", description = "아이디 중복 체크")
    public ResponseEntity<?> checkId(@RequestBody CheckIdDto checkIdDto) {
        if (!authService.checkId(checkIdDto.getUserId())) {
            return ResponseEntity.ok(RestResponse.success("You can use this Id", null));
        } else {
            throw new BusinessException(ErrorCode.DUPLICATE_USERNAME);
        }

    }

    @PostMapping("/register")
    @Operation(summary = "회원가입(*)", description = "회원가입")
    public ResponseEntity<?> register(@RequestBody UserRegisterDto UserRegisterDto) {
        authService.registerUser(UserRegisterDto);
        return ResponseEntity.ok(RestResponse.success("User registered successfully", null));

    }

    @PostMapping("/login")
    @Operation(summary = "로그인(*)", description = "로그인, 엑세스 토큰과, 리프레쉬 토큰(쿠키) 발급")
    public ResponseEntity<?> login(@RequestBody UserLoginDto userLoginDto) {

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
                .body(RestResponse.success("Login Success", responseMap));

    }

    // 로그아웃 엔드포인트
    @PostMapping("/logout")
    @Operation(summary = "로그아웃(*)", description = "로그아웃, 엑세스 토큰과, 리프레쉬 토큰(쿠키) 폐기 및 블랙리스트 추가")
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
            throw new BusinessException(ErrorCode.TOKEN_NOT_PROVIDED);
        }

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
                .body(RestResponse.success("Logout successful", null));

    }

    // 회원 탈퇴 엔드포인트
    @PostMapping("/delete")
    @Operation(summary = "회원탈퇴(*)", description = "회원탈퇴, 엑세스 토큰과, 리프레쉬 토큰(쿠키) 폐기 및 블랙리스트 추가, 회원정보 삭제")
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
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        // JWT 필터에서 인증된 사용자 ID를 가져옵니다.
        String authenticatedUserId = (String) request.getAttribute("authenticatedUserId");
        if (authenticatedUserId == null) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        // 탈퇴 요청의 사용자 ID와 인증된 사용자 ID가 일치하는지 확인
        if (!authenticatedUserId.equals(userDeleteDto.getUserId())) {
            throw new BusinessException(ErrorCode.ACCOUNT_OWNER_MISMATCH);
        }

        authService.deleteUser(userDeleteDto.getUserId(), userDeleteDto.getPassword(), accessToken, refreshToken);
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .maxAge(0)
                .path("/")
                .secure(true)
                .sameSite("None")
                .httpOnly(true)
                .build();

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(RestResponse.success("User deleted successfully", null));
    }

    // 토큰 갱신 엔드포인트 (리프레시 토큰 기반) //수정 필요.
    @PostMapping("/refresh")
    @Operation(summary = "토큰 재발급(*)", description = "엑세스 토큰과, 리프레쉬 토큰(쿠키) 폐기 및 재발급")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {

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
                .body(RestResponse.success("Login Success", responseMap));

    }

    // 아이디 검색 엔드포인트
    @PostMapping("/find")
    @Operation(summary = "회원아이디 찾기(*)", description = "자신의 아이디 마저 잊어버리는 금붕어들을 위한 api")
    public ResponseEntity<?> findUserId(@RequestBody UserFindIdDto userFindIdDto) {

        String userId = authService.findUserId(userFindIdDto.getEmail());
        return ResponseEntity.ok(RestResponse.success("Find user's id successfully", userId));
    }

    // 비밀번호 변경 엔드포인트, 로그인 전 비밀번호 변경을 위해 존재하기 때문에, email과 변경할 password를 받음.

    /* 이메일 인증과 연동되어야함. */

    @PatchMapping("/password")
    @Operation(summary = "회원비밀번호 찾기(*)", description = "찾기라 쓰고, 변경이라 읽는다. 이메일 인증은 언제 추가하지..")
    public ResponseEntity<?> changePassword(@RequestBody UserPasswordDto userPasswordDto) {

        authService.changePassword(userPasswordDto.getUserId(), userPasswordDto.getEmail(),
                userPasswordDto.getPassword());
        return ResponseEntity.ok(RestResponse.success("Password change successfully", null));

    }

    @GetMapping("/email/test")
    public String getMethodName(@RequestParam String param) {
        emailService.sendSimpleEmail(param, "test", "testSend");
        return "success";
    }

}
