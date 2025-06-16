package com.teamproject.sellog.filter;

import java.io.IOException;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.teamproject.sellog.auth.jwt.JwtProvider;
import com.teamproject.sellog.auth.service.AuthService;
import com.teamproject.sellog.common.TokenExtractor;
import com.teamproject.sellog.domain.user.model.user.User;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final AuthService authService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = TokenExtractor.extractTokenFromHeader(request);
        if (token != null) {
            try {
                if (jwtProvider.validateToken(token)) {
                    if (authService.isAccessTokenBlacklisted(token)) {
                        // 블랙리스트에 있으면 유효하지 않은 토큰으로 처리
                        System.err.println("Blacklisted JWT token: " + token); // 로깅
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
                        return; // 요청 처리 중단
                    }
                    System.out.println("필터 토큰 : " + token);
                    System.out.println("필터 클레임 : " + jwtProvider.getClaims(token).get("userId", String.class));
                    String userId = jwtProvider.getClaims(token).get("userId", String.class);
                    Optional<User> userOptional = authService.findByUserId(userId);
                    if (userOptional.isPresent()) {
                        System.out.println("필터 통과");
                        request.setAttribute("authenticatedUserId", userOptional.get().getUserId());
                        request.setAttribute("authenticatedUser", userOptional.get());
                        request.setAttribute("authenticatedUserRole", userOptional.get().getRole());
                    } else {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not found");
                        return;
                    }
                }
            } catch (Exception e) {
                System.err.println("JWT validation failed: " + e.getMessage());
                request.setAttribute("jwtError", e.getMessage());
            }
        }
        filterChain.doFilter(request, response);
    }
}
