package com.teamproject.sellog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class SwaggerConfig {
        @Bean
        public OpenAPI openAPI() {
                String jwt = "JWT";
                SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwt);
                Components components = new Components().addSecuritySchemes(jwt,
                                new SecurityScheme().name(jwt).type(SecurityScheme.Type.HTTP).scheme("bearer")
                                                .bearerFormat("JWT"));
                return new OpenAPI().components(new Components()).info(apiInfo()).addSecurityItem(securityRequirement)
                                .components(components);
        }

        private Info apiInfo() {
                return new Info().title("갈 길이 겁나 먼 Sellog의 REST API 문서")
                                .description(
                                                "\\* 는 동작이 확인된 API, + 는 테스트 필요,  - 표시는 현재 동작 안하는 API" +
                                                                "<br> 2025-08-05 dev_0.0.2 수정내용" +
                                                                "<br> 프로필 조회 와 파일 업로드 관련 api 경로가 변경되었습니다." +
                                                                "<br> 게시글 작성 관련 api는 아직 작성중" +
                                                                "<br> 비공개 계정에 대한 팔로우 승인/거절은 현제 로직 구상중" +
                                                                "<br> 2025-08-08 dev_0.0.3 수정내용" +
                                                                "<br> 게시글 작성 관련 api 중 페이징 처리 목록 부분만 미 작성 상태" +
                                                                "<br> 댓글/리뷰 관련 api와 검색 api 작성 시작" +
                                                                "<br> 비공개 계정에 대한 팔로우 승인/거절은 현제 로직 구상중" +
                                                                "<br> 한글명 파일 업로드 버그 수정" +
                                                                "<br> 2025-08-28 dev_0.0.4 수정내용" +
                                                                "<br> 검색 임시로 만들어둠" +
                                                                "<br> 댓글,리뷰api" +
                                                                "<br> 추천시스템 만드는중")
                                .version("dev_0.0.4");
        }
}
