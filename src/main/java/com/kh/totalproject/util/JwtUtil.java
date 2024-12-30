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
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtUtil {

    private final String secretKey;
    private static final long EXPIRATION_TIME = 1000*60*60;

    public JwtUtil(JwtConfig jwtConfig){
        this.secretKey= jwtConfig.getSecretKey();
    }

    // JWT 생성
    public TokenResponse generateToken(Authentication authentication){
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes());
        long now = (new Date()).getTime();
        Date accessTokenExpiresIn = new Date(now + EXPIRATION_TIME);
        Date refreshTokenExpiresIn = new Date(now + 60 * 60 * 1000 * 24 * 6); // 6일

        String accessToken =  Jwts.builder()
                .subject(String.valueOf(userDetails.getId()))
                .claim("nickname", userDetails.getNickname())
                .claim("authorities", userDetails.getAuthorities())
                .issuedAt(new Date())
                .expiration(accessTokenExpiresIn)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        String refreshToken = Jwts.builder()
                .subject(String.valueOf(userDetails.getId()))
                .claim("nickname", userDetails.getNickname())
                .claim("authorities", userDetails.getAuthorities())
                .issuedAt(new Date())
                .expiration(refreshTokenExpiresIn)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

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
            log.info("JWT 검증 성공");
            return true;
        }
        catch (JwtException | IllegalArgumentException e){
            log.info("JWT 검증 실패 : {}", e.getMessage());
            return false;
        }
    }

}
