package com.kh.totalproject.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Slf4j
@Configuration
public class SwaggerConfig implements WebMvcConfigurer {
    // localhost:8111/swagger-ui/index.html#
    @Bean
    public OpenAPI customOpenAPI(){

        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .name("Authorization")
                .in(SecurityScheme.In.HEADER);

        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("JWT");

        return new OpenAPI()
                .info(new Info()
                        .title("Total Project API")
                        .version("v0")
                        .description("API Swagger"))
                .addSecurityItem(securityRequirement)
                .schemaRequirement("JWT", securityScheme);
    }
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/")
                .setCachePeriod(3600); // 캐시 유지 시간 (초 단위)
    }
//    @Bean
//    public OpenAPI customOpenAPI() {
//        return new OpenAPI()
//                .info(new Info()
//                        .title("Total Project API")
//                        .version("v1.0.0")
//                        .description("API Documentation with Swagger"))
//                .components(new Components()
//                        .addSecuritySchemes("Bearer Authentication",
//                                new SecurityScheme()
//                                        .type(SecurityScheme.Type.HTTP)
//                                        .scheme("bearer")
//                                        .bearerFormat("JWT")))
//                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"));
//    }
//    @Bean
//    public WebMvcConfigurer corsConfigurer() {
//        return new WebMvcConfigurer() {
//            @Override
//            public void addCorsMappings(CorsRegistry registry){
//                registry.addMapping("/v3/api-docs/**").allowedOrigins("*");
//                registry.addMapping("/swagger-ui/**").allowedOrigins("*");
//            }
//        };
//    }
}
