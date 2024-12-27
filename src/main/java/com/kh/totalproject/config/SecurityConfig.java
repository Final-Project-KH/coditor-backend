package com.kh.totalproject.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/v3/api-docs/**", // swagger path
                                "/swagger-ui/**",
                                "/swagger-ui/index.html",
                                "/user/**" // controller path
                        ).permitAll()
                        .anyRequest().authenticated() // 나머지 요청은 인증 필요
                )
                .csrf(csrf -> csrf.disable()); // CSRF 보호 비활성화
//                .headers(headers -> headers // 보안 헤더 비활성화
//                        .frameOptions(frameOptions -> frameOptions.disable()) // X-Frame-Options 비활성화
//                        .contentTypeOptions(contentTypeOptions -> contentTypeOptions.disable())); // X-Content-Type-Options 비활성화

        return http.build();
    }
}
