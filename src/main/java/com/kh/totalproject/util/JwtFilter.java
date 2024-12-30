package com.kh.totalproject.util;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException{
        String jwt = resolveToken(request);
//        HttpServletRequest originalRequest = (HttpServletRequest) request;
//        while (originalRequest instanceof HttpServletRequestWrapper){
//            originalRequest = (HttpServletRequest) ((HttpServletRequestWrapper) originalRequest).getRequest();
//        } // FrontEnd 에서 넘어오는 Header Token 및 URI 및 Header 원상태 검증하는 기능
            // 실제로 넘어올때는 Wrapping 되어서 넘어옴
//        log.info("Original Request URI : {}",originalRequest.getRequestURI()); // FrontEnd 에서 넘어오는 Header Token 검증용
//        log.info("Original Authorization Header: {}", originalRequest.getHeader("Authorization"));
//        log.info("request : {}",request);
//        log.info("token? : {}",jwtUtil.validateToken(jwt));
        if (StringUtils.hasText(jwt) && jwtUtil.validateToken(jwt)){
//           log.info("token?! : {}",jwtUtil.validateToken(jwt));
            Authentication authentication = jwtUtil.getAuthentication(jwt);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }
    
    // Authorization 헤더에서 토큰 추출
    private String resolveToken(HttpServletRequest request){
        String bearerToken = request.getHeader("Authorization");
//        log.info("request : {}", request);
//        log.info("bearerToken : {}", bearerToken);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer")){
            return bearerToken.substring(7);
        }
        return null;
    }
}
