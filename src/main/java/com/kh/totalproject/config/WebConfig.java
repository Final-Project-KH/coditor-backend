package com.kh.totalproject.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// 기본 CORS 설정 (Security CORS 설정은 따로 필요함)
// Controller 에 "http://localhost:3000" 명시할 필요 없이 바로 / Start 하면 됨
// Flask url / React url path를 잘 구분하는 것이 중요
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings (CorsRegistry registry){
        registry.addMapping("/**") // 모든 엔드포인트 허용
                .allowedOrigins(
                        "http://localhost:3000", // React 개발 서버
                        "http://localhost:5000" // Flask 서버
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
