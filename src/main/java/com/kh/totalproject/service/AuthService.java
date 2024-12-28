package com.kh.totalproject.service;

import com.kh.totalproject.dto.request.LoginRequest;
import com.kh.totalproject.dto.response.TokenResponse;
import com.kh.totalproject.repository.UserRepository;
import com.kh.totalproject.util.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final AuthenticationManagerBuilder managerBuilder;

    public boolean isUser(LoginRequest loginRequest){
        return userRepository.existsByEmailAndPassword(loginRequest.getEmail(), loginRequest.getPassword());
    }

    public TokenResponse login(LoginRequest loginRequest){
        if (isUser(loginRequest)){
            UsernamePasswordAuthenticationToken authenticationToken = loginRequest.toAuthentication();
            Authentication authentication = managerBuilder.getObject().authenticate(authenticationToken);
            return jwtUtil.generateToken(authentication);
        }
        else log.warn("회원 정보가 일치하지 않습니다.");
        return null;
    }
}
