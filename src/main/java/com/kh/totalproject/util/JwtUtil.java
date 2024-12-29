package com.kh.totalproject.util;


import com.kh.totalproject.config.JwtConfig;
import com.kh.totalproject.dto.response.TokenResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

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


        String accessToken =  Jwts.builder()
                .subject(String.valueOf(userDetails.getId()))
                .claim("nickname", userDetails.getNickname())
                .claim("authorities", userDetails.getAuthorities())
                .issuedAt(new Date())
                .expiration(accessTokenExpiresIn)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
        return TokenResponse.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .tokenExpiresIn(accessTokenExpiresIn.getTime())
                .build();
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
            return true;
        }
        catch (JwtException | IllegalArgumentException e){
            return false;
        }
    }

}
