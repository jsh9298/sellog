package com.teamproject.sellog.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.teamproject.sellog.filter.LogFilter;
import com.teamproject.sellog.interceptor.LogInterCeptor;

import io.jsonwebtoken.lang.Arrays;

public class WebConfig implements WebMvcConfigurer {

    private static final String[] FILTER_PATHS = {
            "",
    };

    @Bean
    public FilterRegistrationBean filterBean() {
        FilterRegistrationBean registrationBean = new FilterRegistrationBean<>(new LogFilter());
        registrationBean.setOrder(0);
        registrationBean.setUrlPatterns(Arrays.asList(FILTER_PATHS));
        return registrationBean;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LogInterCeptor()).excludePathPatterns(FILTER_PATHS);
    }
}
