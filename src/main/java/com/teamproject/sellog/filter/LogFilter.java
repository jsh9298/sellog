package com.teamproject.sellog.filter;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/*
init()
웹 컨테이너(톰캣)이 시작될 때 필터 최초 한 번 인스턴스 생성

doFilter()
클라이언트의 요청 시 전/후 처리
FilterChain을 통해 전달

public void destroy()
필터 인스턴스가 제거될 때 실행되는 메서드, 종료하는 기능
 */

@Slf4j
public class LogFilter implements Filter {

    /*
     * - 필터 인스턴스 초기화
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("Filter logging start");
    }

    /*
     * - 전/후 처리
     * - Request, Response가 필터를 거칠 때 수행되는 메소드
     * - chain.doFilter() 기점으로 request, response 나눠집니다.
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String requestURI = req.getRequestURI();

        log.info("---Request(" + requestURI + ") 필터---");
        chain.doFilter(request, response);
        log.info("---Response(" + requestURI + ") 필터---");
    }

    /*
     * - 필터 인스턴스 종료
     */
    @Override
    public void destroy() {
        log.info("Filter log end");
    }
}
