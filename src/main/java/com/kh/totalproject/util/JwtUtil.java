package com.kh.totalproject.util;


import com.kh.totalproject.config.JwtConfig;
import com.kh.totalproject.dto.response.TokenResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

@Slf4j
@Component
// 기존 강의의 TokenProvider 역할
public class JwtUtil {

    private final String secretKey;
    private static final long EXPIRATION_TIME = 1000*60*60;

    // Secret key 받아오기
    public JwtUtil(JwtConfig jwtConfig){
        this.secretKey= jwtConfig.getSecretKey();
    }

    // JWT 생성
    public TokenResponse generateToken(Authentication authentication){
        // Login 시 인증 권한 요청한 아이디 authentication 기반으로 CustomUserDetails 생성
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes());
        long now = (new Date()).getTime();
        Date accessTokenExpiresIn = new Date(now + EXPIRATION_TIME);
        Date refreshTokenExpiresIn = new Date(now + 60 * 60 * 1000 * 24 * 6); // 6일
        
        // Access Token 생성
        // 만기 시간 : 1시간
        String accessToken =  Jwts.builder() 
                .subject(String.valueOf(userDetails.getId())) // sub : Long id (이 정보를 기반으로 앞으로 관련 정보 탐색)
                .claim("nickname", userDetails.getNickname()) // claim : String nickname
                .claim("authorities", userDetails.getAuthorities()) // claim : 권한 (User Or Admin)
                .issuedAt(new Date())
                .expiration(accessTokenExpiresIn)
                .signWith(key, SignatureAlgorithm.HS256) // 우선 HS256으로 적용
                .compact();
        
        // Refresh Token 생성
        // 만기 시간 : 6일
        String refreshToken = Jwts.builder()
                .subject(String.valueOf(userDetails.getId()))
                .claim("nickname", userDetails.getNickname())
                .claim("authorities", userDetails.getAuthorities())
                .issuedAt(new Date())
                .expiration(refreshTokenExpiresIn)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
        
        // 설정한 TokenResponse 형태에 맞춰서 변환
        return TokenResponse.builder() 
                .grantType("Bearer")
                .accessToken(accessToken)
                .accessTokenExpiresIn(accessTokenExpiresIn.getTime())
                .refreshToken(refreshToken)
                .refreshTokenExpiresIn(refreshTokenExpiresIn.getTime())
                .build();
    }

    public Authentication getAuthentication(String token){
        Claims claims = parseToken(token);

        Collection<? extends GrantedAuthority> authorities=
                Arrays.stream(claims.get("authorities").toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .toList();

        // UserID 대신 UserKey 넣어서 생성
        User principal = new User(claims.getSubject(), "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    public Claims parseToken(String token){
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes());
        return Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validateToken(String token){
        try{
            parseToken(token);
            log.info("JWT 검증 성공"); // Debug 용
            return true;
        }
        catch (JwtException | IllegalArgumentException e){
            log.info("JWT 검증 실패 : {}", e.getMessage()); // Debug 용
            return false;
        }
    }

}
