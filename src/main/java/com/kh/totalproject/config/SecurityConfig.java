package com.kh.totalproject.config;


import com.kh.totalproject.util.JwtAccessDeniedHandler;
import com.kh.totalproject.util.JwtAuthenticationEntryPoint;
import com.kh.totalproject.util.JwtFilter;
import com.kh.totalproject.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {
    private final JwtUtil jwtUtil;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    
    // Swagger 접근 Issue
    // 1. 운영 환경에서는 Swagger 비활성화
    // 2. Spring Security 로 관리자만 접근 허용
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http
                // Security Cors 정책 -> React / Flask
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // Spring Security CSRF(Cross-Site Request Forgery) 비활성화 -> 대신 JWT 인증으로 대체
                .csrf(AbstractHttpConfigurer::disable)
                // 요청 인증 및 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // 특정 URL 패턴에 대해 접근 허용 (permitAll)
                        .requestMatchers(
                                "/v3/api-docs/**", // swagger path
                                "/swagger-ui/**",
                                "/swagger-ui/index.html",
                                "/auth/**" // auth 이외에는 전부 JWT 인증 권한 요구?
                                // 회원이 아니더라도 이동 가능한 페이지 정해야함
                        ).permitAll()
                        .anyRequest().authenticated() // 나머지 요청은 인증 필요

                )
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler))
                // JWT 인증 필터 추가
                // 요청이 UsernamePasswordAuthenticationFilter 전에 JWT 필터를 통해 인증되도록 설정
                .addFilterBefore(new JwtFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);
//                .headers(headers -> headers // 보안 헤더 비활성화
//                        .frameOptions(frameOptions -> frameOptions.disable()) // X-Frame-Options 비활성화
//                        .contentTypeOptions(contentTypeOptions -> contentTypeOptions.disable())); // X-Content-Type-Options 비활성화

        return http.build();
    }
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin("http://localhost:3000"); // 허용할 Origin
        configuration.addAllowedOrigin("http://localhost:5000");
        configuration.addAllowedMethod("*"); // 모든 HTTP 메서드 허용
        configuration.addAllowedHeader("*"); // 모든 헤더 허용
        configuration.setAllowCredentials(true); // 쿠키 허용

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // 모든 경로에 대해 설정
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
