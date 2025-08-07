package com.teamproject.sellog.filter;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.teamproject.sellog.common.accountsUtils.TokenExtractor;
import com.teamproject.sellog.domain.auth.model.jwt.JwtProvider;
import com.teamproject.sellog.domain.auth.service.AuthService;
import com.teamproject.sellog.domain.user.model.entity.user.User;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebFilter(urlPatterns = { "/api/**", "/auth/delete" })
@Component
@Order(2)
public class JwtAuthFilter implements Filter {

    private final JwtProvider jwtProvider;
    private final AuthService authService;

    public JwtAuthFilter(JwtProvider jwtProvider, AuthService authService) {
        this.authService = authService;
        this.jwtProvider = jwtProvider;
    }

    private final List<Map.Entry<String, Pattern>> NofilteringURI = Arrays.asList(
            new AbstractMap.SimpleEntry<>("GET", Pattern.compile("^/api/[^/]+/(preview)$")));

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String reqURI = req.getRequestURI();
        String reqMethod = req.getMethod();

        for (Map.Entry<String, Pattern> entry : NofilteringURI) { // 또는 HttpMethodAndUriPattern
            String excludedMethod = entry.getKey(); // 또는 entry.getHttpMethod()
            Pattern excludedPattern = entry.getValue(); // 또는 entry.getUriPattern()

            if (reqMethod.equalsIgnoreCase(excludedMethod) && excludedPattern.matcher(reqURI).matches()) {
                chain.doFilter(request, response);
                return; // 필터 로직 종료
            }
        }

        String token = TokenExtractor.extractTokenFromHeader(req);
        if (token != null) {
            try {
                if (jwtProvider.validateToken(token)) {
                    if (authService.isAccessTokenBlacklisted(token)) {
                        // 블랙리스트에 있으면 유효하지 않은 토큰으로 처리
                        System.err.println("Blacklisted JWT token: " + token); // 로깅
                        res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
                        return; // 요청 처리 중단
                    }
                    String userId = jwtProvider.getClaims(token).get("userId", String.class);
                    Optional<User> userOptional = authService.findByUserId(userId);
                    if (userOptional.isPresent()) {
                        request.setAttribute("authenticatedUserId", userOptional.get().getUserId());
                        request.setAttribute("authenticatedUser", userOptional.get());
                        request.setAttribute("authenticatedUserRole", userOptional.get().getRole());
                        request.setAttribute("authenticatedUserAccountState", userOptional);
                        request.setAttribute("authenticatedUserStatus", userOptional.get().getAccountStatus());
                        request.setAttribute("authenticatedUserVisibility", userOptional.get().getAccountVisibility());
                    } else {
                        res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not found");
                        return;
                    }
                }
            } catch (Exception e) {
                System.err.println("JWT validation failed: " + e.getMessage());
                request.setAttribute("jwtError", e.getMessage());
            }
        }
        chain.doFilter(request, response);
    }
}
