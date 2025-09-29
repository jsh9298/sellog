package com.teamproject.sellog.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // URL 경로가 /media/** 로 시작하면, 로컬의 uploadDir 경로에서 파일을 찾아 제공합니다.
        // 예: http://localhost:8080/media/user123/origin/my-image.jpg ->
        // {프로젝트경로}/uploads/user123/origin/my-image.jpg
        registry.addResourceHandler("/media/**")
                .addResourceLocations("file:" + uploadDir + "/");
    }
}
