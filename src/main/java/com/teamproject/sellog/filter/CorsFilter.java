package com.teamproject.sellog.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebFilter(urlPatterns = { "/**" })
@Component
@Order(1)
public class CorsFilter implements Filter {
    private final List<String> allowedOrigins = Arrays.asList(
            "http://4.230.10.171",
            "http://localhost:3000",
            "http://localhost:5173" // 개발 환경을 위한 로컬 호스트
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String origin = req.getHeader("Origin");
        if (origin != null && allowedOrigins.contains(origin)) {
            res.setHeader("Access-Control-Allow-Origin", origin); // 요청 Origin을 그대로 응답
        }
        res.setHeader("Access-Control-Allow-Credentials", "true"); // 얘 true로
        // 되있으면 origin은 무조건 명시해야함. * 로 쌈싸먹는거 안됨
        res.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, PATCH, OPTIONS");
        res.setHeader("Access-Control-Max-Age", "3600");
        res.setHeader("Access-Control-Allow-Headers",
                "Origin, X-Requested-With, Content-Type, Accept, Authorization");
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            res.setStatus(HttpServletResponse.SC_OK);
        } else {
            chain.doFilter(request, response);
        }
    }

}
