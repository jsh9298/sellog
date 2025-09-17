package com.teamproject.sellog.domain.auth.controller;

import com.teamproject.sellog.common.accountsUtils.TokenExtractor;
import com.teamproject.sellog.common.emailUtils.EmailSendDto;
import com.teamproject.sellog.common.emailUtils.EmailService;
import com.teamproject.sellog.common.responseUtils.BusinessException;
import com.teamproject.sellog.common.responseUtils.ErrorCode;
import com.teamproject.sellog.common.responseUtils.RestResponse;
import com.teamproject.sellog.domain.auth.model.dto.request.CheckIdDto;
import com.teamproject.sellog.domain.auth.model.dto.request.UserDeleteDto;
import com.teamproject.sellog.domain.auth.model.dto.request.UserFindIdDto;
import com.teamproject.sellog.domain.auth.model.dto.request.UserOtpRequestDto;
import com.teamproject.sellog.domain.auth.model.dto.request.UserOtpVerifyDto;
import com.teamproject.sellog.domain.auth.model.dto.request.UserLoginDto;
import com.teamproject.sellog.domain.auth.model.dto.request.UserPasswordDto;
import com.teamproject.sellog.domain.auth.model.dto.request.UserRegisterDto;
import com.teamproject.sellog.domain.auth.model.dto.response.UserLoginResponse;
import com.teamproject.sellog.domain.auth.model.jwt.JWT;
import com.teamproject.sellog.domain.auth.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/auth")
@Tag(name = "회원관련", description = "회원정보 관련 api")
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
    @Operation(summary = "아이디 중복 체크(*)", description = "아이디 중복 체크")
    public ResponseEntity<?> checkId(@RequestBody CheckIdDto checkIdDto) {
        authService.checkId(checkIdDto.getUserId());
        return ResponseEntity.ok(RestResponse.success("You can use this Id", null));
    }

    @PostMapping("/register/send")
    public ResponseEntity<?> registerSend(@RequestBody UserOtpRequestDto dto) {
        authService.sendOtpForPasswordReset(dto);
        return ResponseEntity.ok(RestResponse.success("OTP가 이메일로 발송되었습니다.", null));
    }

    @PostMapping("/register/verify")
    public ResponseEntity<?> registerVerify(@RequestBody UserOtpVerifyDto dto) {
        authService.verifyOtp(dto);
        return ResponseEntity.ok(RestResponse.success("OTP 인증에 성공했습니다.", null));
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

    // 토큰 갱신 엔드포인트 (리프레시 토큰 기반)
    @PostMapping("/refresh")
    @Operation(summary = "토큰 재발급(*)", description = "엑세스 토큰과, 리프레쉬 토큰(쿠키) 폐기 및 재발급")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {

        String accessToken = TokenExtractor.extractTokenFromHeader(request); // 헤더에서 엑세스토큰 추출
        String refreshToken = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) { // 쿠키 이름이 "refreshToken"인지 확인
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        JWT jwtTokens = authService.refreshToken(accessToken, refreshToken);

        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("accessToken", jwtTokens.getAccessToken());

        ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.ok();

        // RefreshToken이 재발급된 경우에만 쿠키를 설정
        if (jwtTokens.getRefreshToken() != null) {
            ResponseCookie cookie = ResponseCookie.from("refreshToken", jwtTokens.getRefreshToken())
                    .maxAge(refreshTokenValidityInMilliseconds)
                    .path("/").secure(true).sameSite("None").httpOnly(true).build();
            responseBuilder.header(HttpHeaders.SET_COOKIE, cookie.toString());
        }

        return responseBuilder.body(RestResponse.success("Token refreshed successfully", responseMap));

    }

    // 아이디 검색 엔드포인트
    @PostMapping("/find")
    @Operation(summary = "회원아이디 찾기(*)", description = "자신의 아이디 마저 잊어버리는 금붕어들을 위한 api")
    public ResponseEntity<?> findUserId(@RequestBody UserFindIdDto userFindIdDto) {

        String userId = authService.findUserId(userFindIdDto.getEmail());
        return ResponseEntity.ok(RestResponse.success("Find user's id successfully", userId));
    }

    @PostMapping("/password/send")
    @Operation(summary = "비밀번호 재설정 OTP 발송(+)", description = "아이디와 이메일이 일치하는 사용자에게 OTP를 발송합니다.")
    public ResponseEntity<?> sendOtpForPasswordReset(@RequestBody UserOtpRequestDto dto) {
        authService.sendOtpForPasswordReset(dto);
        return ResponseEntity.ok(RestResponse.success("OTP가 이메일로 발송되었습니다.", null));
    }

    @PostMapping("/password/verify")
    @Operation(summary = "비밀번호 재설정 OTP 검증(+)", description = "발송된 OTP를 검증합니다. 성공 시 비밀번호 변경이 가능해집니다.")
    public ResponseEntity<?> verifyOtp(@RequestBody UserOtpVerifyDto dto) {
        authService.verifyOtp(dto);
        return ResponseEntity.ok(RestResponse.success("OTP 인증에 성공했습니다.", null));
    }

    @PatchMapping("/password")
    @Operation(summary = "회원비밀번호 변경(+)", description = "OTP 인증 후 새로운 비밀번호로 변경합니다.")
    public ResponseEntity<?> changePassword(@RequestBody UserPasswordDto userPasswordDto) {
        // OTP 인증이 성공했는지 확인하는 로직이 서비스 내부에 포함되어야 합니다.
        // (예: Redis에 인증 성공 플래그 저장 후 확인)
        authService.changePassword(userPasswordDto.getUserId(), userPasswordDto.getPassword());
        return ResponseEntity.ok(RestResponse.success("Password change successfully", null));
    }
}
