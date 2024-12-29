package com.kh.totalproject.service;

import com.kh.totalproject.dto.request.LoginRequest;
import com.kh.totalproject.dto.response.TokenResponse;
import com.kh.totalproject.entity.User;
import com.kh.totalproject.repository.UserRepository;
import com.kh.totalproject.util.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final AuthenticationManagerBuilder managerBuilder;
    private final PasswordEncoder passwordEncoder;


    public TokenResponse login(LoginRequest loginRequest){
        User user = userRepository.findByUserId(loginRequest.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("등록된 아이디가 없습니다."));
        log.info("PasswordEncoder : {}", passwordEncoder.getClass());
        log.info("입력된 비밀번호 : {}", loginRequest.getPassword());
        log.info("저장된 암호화된 비밀번호 : {}", user.getPassword());
        boolean isPasswordMatch = passwordEncoder.matches(loginRequest.getPassword(), user.getPassword());
        log.info("비밀번호 일치 여부: {}", isPasswordMatch);

        if (passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())){
            UsernamePasswordAuthenticationToken authenticationToken = loginRequest.toAuthentication();
            Authentication authentication = managerBuilder.getObject().authenticate(authenticationToken);
            return jwtUtil.generateToken(authentication);
        }
        else log.warn("회원 정보가 일치하지 않습니다.");
        return null;
    }
}
