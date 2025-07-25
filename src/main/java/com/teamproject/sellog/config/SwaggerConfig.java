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
                new SecurityScheme().name(jwt).type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT"));
        return new OpenAPI().components(new Components()).info(apiInfo()).addSecurityItem(securityRequirement)
                .components(components);
    }

    private Info apiInfo() {
        return new Info().title("갈길이 겁나 먼 Sellog의 REST API 문서")
                .description("\\* 는 동작이 확인된 API, + 는 테스트 필요,  - 표시는 현재 동작 안하는 API")
                .version("0.0.1");
    }
}
