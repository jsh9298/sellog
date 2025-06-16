package com.teamproject.sellog.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.teamproject.sellog.filter.CorsFilter;
import com.teamproject.sellog.filter.JwtAuthFilter;

import io.jsonwebtoken.lang.Arrays;

@Configuration
public class WebConfig {
    private final JwtAuthFilter jwtAuthFilter;
    private final CorsFilter corsFilter;

    public WebConfig(JwtAuthFilter jwtAuthFilter, CorsFilter corsFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.corsFilter = corsFilter;
    }

    private static final String[] FILTER_PATHS = {
            "/api/*", "/auth/delete"
    };

    @Bean
    public FilterRegistrationBean<JwtAuthFilter> authfilterBean() {
        FilterRegistrationBean<JwtAuthFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(jwtAuthFilter);
        registrationBean.setOrder(1);
        registrationBean.setUrlPatterns(Arrays.asList(FILTER_PATHS));
        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean<CorsFilter> corsfilterBean() {
        FilterRegistrationBean<CorsFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(corsFilter);
        registrationBean.setOrder(0);
        registrationBean.addUrlPatterns("/**");
        return registrationBean;
    }
}
